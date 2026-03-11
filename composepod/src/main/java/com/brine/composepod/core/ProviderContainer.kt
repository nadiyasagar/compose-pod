package com.brine.composepod.core

import com.brine.composepod.mvi.StateNotifier
import com.brine.composepod.mvi.StateNotifierProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

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
    overrides: List<Override> = emptyList(),
    private val observers: List<ProviderObserver> = emptyList()
) {
    private val nodes = mutableMapOf<ProviderBase<*>, ProviderNode<*>>()
    private val overrideMap: Map<ProviderBase<*>, ProviderBase<*>> =
        overrides.associate { it.origin to it.override }

    @Suppress("UNCHECKED_CAST")
    fun <T> read(provider: ProviderBase<T>): T {
        return getNode(provider).value as T
    }
    
    @Suppress("UNCHECKED_CAST")
    internal fun <T> getNode(provider: ProviderBase<T>): ProviderNode<T> {
        val actualProvider = (overrideMap[provider] as? ProviderBase<T>) ?: provider
        
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
        if (provider is AutoDisposeProvider<*>) {
            val node = nodes[provider]
            if (node != null && node.listenerCount == 0 && node.getDependents().isEmpty()) {
                invalidate(provider)
            }
        }
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

    var value: Any? = null
        private set

    private val onDisposeCallbacks = mutableListOf<() -> Unit>()
    
    // Providers that this node is watching. (If they change, we invalidate ourselves)
    private val dependencies = mutableSetOf<ProviderBase<*>>()
    
    // Providers that are watching this node. (If we change, we invalidate them)
    private val dependents = mutableSetOf<ProviderBase<*>>()
    
    var listenerCount = 0
        private set

    fun initialize() {
        // Run the provider block, which may call this.watch() or this.read()
        value = provider.create(this)
    }

    fun addListener() {
        listenerCount++
    }
    
    fun removeListener() {
        listenerCount--
        if (listenerCount <= 0) {
            listenerCount = 0
            container.checkAutoDispose(provider)
        }
    }

    override fun <R> read(provider: ProviderBase<R>): R {
        return container.read(provider)
    }
    
    override fun <R> watch(provider: ProviderBase<R>): R {
        val watchedNode = container.getNode(provider)
        
        dependencies.add(provider)
        watchedNode.dependents.add(this.provider)
        
        return watchedNode.value as R
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

    fun getDependents(): Set<ProviderBase<*>> = dependents.toSet()

    fun dispose() {
        coroutineScope.cancel()
        onDisposeCallbacks.forEach { it() }
        onDisposeCallbacks.clear()
        
        // Remove ourselves from dependencies
        dependencies.forEach { dep ->
            container.getNode(dep).dependents.remove(this.provider)
        }
        dependencies.clear()
        dependents.clear()
    }
}
