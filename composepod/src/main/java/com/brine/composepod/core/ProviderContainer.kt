package com.brine.composepod.core

import com.brine.composepod.mvi.StateNotifier
import com.brine.composepod.mvi.StateNotifierProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class Override(val origin: ProviderBase<*>, val override: ProviderBase<*>)

/**
 * An observer that listens to the lifecycle of providers.
 */
interface ProviderObserver {
    fun <T> didAddProvider(provider: ProviderBase<T>, value: T, container: ProviderContainer) {}
    fun <T> didUpdateProvider(provider: ProviderBase<T>, previousValue: T?, newValue: T, container: ProviderContainer) {}
    fun <T> didDisposeProvider(provider: ProviderBase<T>, container: ProviderContainer) {}
}

fun <T> Provider<T>.overrideWith(create: (ProviderRef) -> T): Override {
    return Override(this, Provider(name = this.name, createFn = create))
}

/**
 * The container that stores the state of all providers.
 * Must be created once per application or scope.
 */
class ProviderContainer(
    private val parent: ProviderContainer? = null,
    overrides: List<Override> = emptyList(),
    private val observers: List<ProviderObserver> = emptyList()
) {
    private val nodes = ConcurrentHashMap<ProviderBase<*>, ProviderNode<*>>()
    private val overrideMap: Map<ProviderBase<*>, ProviderBase<*>> =
        overrides.associate { it.origin to it.override }

    @Suppress("UNCHECKED_CAST")
    fun <T> read(provider: ProviderBase<T>): T {
        return getNode(provider).value as T
    }
    
    @Suppress("UNCHECKED_CAST")
    internal fun <T> getNode(provider: ProviderBase<T>): ProviderNode<T> {
        val actualProvider = (overrideMap[provider] as? ProviderBase<T>) ?: provider
        
        // If not overridden here and we have a parent, try to get from parent
        if (!overrideMap.containsKey(provider) && parent != null) {
            return parent.getNode(provider)
        }

        return nodes.getOrPut(actualProvider) {
            val newNode = ProviderNode(actualProvider, this)
            newNode.initialize()
            observers.forEach { it.didAddProvider(actualProvider, newNode.value as T, this) }
            newNode
        } as ProviderNode<T>
    }

    /**
     * Forcefully destroys a provider's state, clearing memory and notifying observers.
     * Also invalidates all dependents watching this provider.
     */
    fun invalidate(provider: ProviderBase<*>) {
        val actualProvider = overrideMap[provider] ?: provider
        nodes.remove(actualProvider)?.let { node ->
            val depsToInvalidate = node.getDependents()
            node.dispose()
            observers.forEach { it.didDisposeProvider(actualProvider, this) }

            // Cascade invalidation to anything watching this provider
            depsToInvalidate.forEach { invalidate(it) }
        }
    }
    
    internal fun checkAutoDispose(provider: ProviderBase<*>) {
        if (provider is AutoDisposeProvider<*> && provider !is KeepAliveProvider<*>) {
            val node = nodes[provider]
            if (node != null && node.listenerCount == 0 && node.getDependents().isEmpty()) {
                invalidate(provider)
            }
        }
    }

    /**
     * Refreshes a provider by re-executing its create function without destroying it.
     * Unlike invalidate(), this preserves dependents and only updates the value.
     * Returns true if provider exists and was refreshed, false otherwise.
     */
    fun refresh(provider: ProviderBase<*>): Boolean {
        val actualProvider = overrideMap[provider] ?: provider
        val node = nodes[actualProvider] ?: return false

        @Suppress("UNCHECKED_CAST")
        val oldValue = node.value
        node.refresh()
        observers.forEach { observer ->
            @Suppress("UNCHECKED_CAST")
            observer.didUpdateProvider(
                actualProvider as ProviderBase<Any?>,
                oldValue,
                node.value,
                this
            )
        }
        return true
    }

    /**
     * Cleans up all providers.
     */
    fun dispose() {
        nodes.keys.toList().forEach { invalidate(it) }
        nodes.clear()
    }
}

class ProviderNode<T>(
    private val provider: ProviderBase<T>,
    private val container: ProviderContainer
) : ProviderRef {

    override val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override var currentValue: Any? = null
        private set

    var value: Any?
        get() = currentValue
        private set(v) {
            val changed = currentValue != v
            currentValue = v
            if (changed) {
                @Suppress("UNCHECKED_CAST")
                onValueChangeListeners.forEach { it(v as T) }
            }
        }

    private val onDisposeCallbacks = CopyOnWriteArrayList<() -> Unit>()
    private val dependencyCleanups = CopyOnWriteArrayList<() -> Unit>()
    private val onCancelCallbacks = CopyOnWriteArrayList<() -> Unit>()
    private val onResumeCallbacks = CopyOnWriteArrayList<() -> Unit>()
    private val onValueChangeListeners = CopyOnWriteArrayList<(T) -> Unit>()

    // Providers that this node is watching. (If they change, we invalidate ourselves)
    private val dependencies = ConcurrentHashMap.newKeySet<ProviderBase<*>>()

    // Providers that are watching this node. (If we change, we invalidate them)
    internal val dependents = ConcurrentHashMap.newKeySet<ProviderBase<*>>()
    
    private var isInitializing = false
    
    private val _listenerCount = AtomicInteger(0)
    val listenerCount: Int
        get() = _listenerCount.get()

    fun initialize() {
        if (isInitializing) {
            throw IllegalStateException("Circular dependency detected while initializing provider: ${provider.name ?: provider}")
        }
        isInitializing = true
        try {
            value = provider.create(this)
        } finally {
            isInitializing = false
        }
    }

    /**
     * Re-executes the provider create function without destroying the node.
     * Preserves all dependents.
     */
    fun refresh() {
        // Clear previous dependencies before re-creating
        clearDependencies()
        initialize()
    }

    private fun clearDependencies() {
        // Run and clear all dependency-bound listeners
        dependencyCleanups.forEach { it() }
        dependencyCleanups.clear()
        
        dependencies.forEach { dep ->
            container.getNode(dep).dependents.remove(this.provider)
        }
        dependencies.clear()
    }

    fun addListener() {
        val wasInactive = _listenerCount.get() == 0
        _listenerCount.incrementAndGet()
        if (wasInactive) {
            onResumeCallbacks.forEach { it() }
        }
    }
    
    fun removeListener() {
        if (_listenerCount.decrementAndGet() <= 0) {
            _listenerCount.set(0)
            onCancelCallbacks.forEach { it() }
            container.checkAutoDispose(provider)
        }
    }

    override fun <R> read(provider: ProviderBase<R>): R {
        return container.read(provider)
    }
    
    override fun <R> watch(provider: ProviderBase<R>): R {
        val watchedNode = container.getNode(provider)
        
        if (dependencies.add(provider)) {
            watchedNode.dependents.add(this.provider)
            
            // When the watched node changes, we invalidate this node
            val listener: (R) -> Unit = { 
                invalidate(this.provider) 
            }
            watchedNode.addOnValueChangeListener(listener)
            
            // Register cleanup specifically for this dependency 
            // to be removed during refresh or disposal
            dependencyCleanups.add { 
                watchedNode.removeOnValueChangeListener(listener) 
            }
        }

        return watchedNode.value as R
    }
    
    override fun <R> listen(provider: ProviderBase<R>, listener: (R?, R) -> Unit) {
        val watchedNode = container.getNode(provider)
        var previousValue: R? = null
        
        val valueListener: (R) -> Unit = { newValue ->
            listener(previousValue, newValue)
            previousValue = newValue
        }
        
        watchedNode.addOnValueChangeListener(valueListener)
        onDispose { watchedNode.removeOnValueChangeListener(valueListener) }
    }

    override fun invalidate(provider: ProviderBase<*>) {
        container.invalidate(provider)
    }

    override fun <T, R> select(
        provider: ProviderBase<out StateFlow<T>>,
        selector: (T) -> R
    ): R {
        val flow = container.read(provider)
        return selector(flow.value)
    }

    override fun <Notifier : StateNotifier<S>, S, R> select(
        provider: StateNotifierProvider<Notifier, S>,
        selector: (S) -> R
    ): R {
        val notifier = container.read(provider)
        return selector(notifier.stateFlow.value)
    }

    override fun onDispose(callback: () -> Unit) {
        onDisposeCallbacks.add(callback)
    }

    override fun onCancel(callback: () -> Unit) {
        onCancelCallbacks.add(callback)
    }

    override fun onResume(callback: () -> Unit) {
        onResumeCallbacks.add(callback)
    }

    fun addOnValueChangeListener(listener: (T) -> Unit) {
        onValueChangeListeners.add(listener)
    }

    fun removeOnValueChangeListener(listener: (T) -> Unit) {
        onValueChangeListeners.remove(listener)
    }

    fun getDependents(): Set<ProviderBase<*>> = dependents.toSet()

    fun dispose() {
        coroutineScope.cancel()
        onDisposeCallbacks.forEach { it() }
        onDisposeCallbacks.clear()
        dependencyCleanups.forEach { it() }
        dependencyCleanups.clear()
        
        // Remove ourselves from dependencies
        dependencies.forEach { dep ->
            container.getNode(dep).dependents.remove(this.provider)
        }
        dependencies.clear()
        dependents.clear()
    }
}
