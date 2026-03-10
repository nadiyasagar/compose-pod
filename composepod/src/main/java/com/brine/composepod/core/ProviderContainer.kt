package com.brine.composepod.core

import com.brine.composepod.mvi.StateNotifier
import com.brine.composepod.mvi.StateNotifierProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

class Override(val origin: ProviderBase<*>, val override: ProviderBase<*>)

fun <T> Provider<T>.overrideWith(create: (ProviderRef) -> T): Override {
    return Override(this, Provider(name = this.name, createFn = create))
}

/**
 * The container that stores the state of all providers.
 * Must be created once per application or scope.
 */
class ProviderContainer(
    overrides: List<Override> = emptyList()
) {
    private val nodes = mutableMapOf<ProviderBase<*>, ProviderNode<*>>()
    private val overrideMap: Map<ProviderBase<*>, ProviderBase<*>> =
        overrides.associate { it.origin to it.override }

    @Suppress("UNCHECKED_CAST")
    fun <T> read(provider: ProviderBase<T>): T {
        val actualProvider = (overrideMap[provider] as? ProviderBase<T>) ?: provider
        
        val node = nodes.getOrPut(actualProvider) {
            val newNode = ProviderNode(actualProvider, this)
            newNode.initialize()
            newNode
        }
        return node.value as T
    }

    /**
     * Cleans up all providers.
     */
    fun dispose() {
        nodes.values.forEach { it.dispose() }
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

    fun initialize() {
        value = provider.create(this)
    }

    override fun <R> read(provider: ProviderBase<R>): R {
        return container.read(provider)
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

    fun dispose() {
        coroutineScope.cancel()
        onDisposeCallbacks.forEach { it() }
        onDisposeCallbacks.clear()
    }
}
