package com.brine.composepod.core
import com.brine.composepod.async.AsyncState
import com.brine.composepod.mvi.StateNotifier
import com.brine.composepod.mvi.StateNotifierProvider
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Interface marking a provider that should have its state destroyed
 * when no longer being listened to by the UI or other providers.
 */
interface AutoDisposeProvider<T> {
    val origin: ProviderBase<T>
}

/**
 * Interface marking a provider that should be kept alive even with autoDispose.
 */
interface KeepAliveProvider<T> {
    val origin: ProviderBase<T>
}

@JvmName("autoDisposeBase")
fun <T> ProviderBase<T>.autoDispose(): ProviderBase<T> {
    val origin = this
    return object : ProviderBase<T>(origin.name?.let { "$it-autoDispose" }), AutoDisposeProvider<T> {
        override val origin: ProviderBase<T> = origin
        
        override fun create(ref: ProviderRef): T {
            return origin.create(ref)
        }
    }
}

@JvmName("keepAliveBase")
fun <T> ProviderBase<T>.keepAlive(): ProviderBase<T> {
    val origin = this
    return object : ProviderBase<T>(origin.name?.let { "$it-keepAlive" }), KeepAliveProvider<T> {
        override val origin: ProviderBase<T> = origin
        
        override fun create(ref: ProviderRef): T {
            return origin.create(ref)
        }
    }
}

/**
 * A provider builder that takes an argument to create parameterized providers.
 */
class ProviderFamily<Arg, T>(
    private val name: String? = null,
    private val builder: (ProviderRef, Arg) -> T
) {
    // Cache the generated providers using WeakReference to avoid memory leaks
    private val cachedProviders = ConcurrentHashMap<Arg, WeakReference<Provider<T>>>()

    operator fun invoke(arg: Arg): Provider<T> {
        val ref = cachedProviders[arg]
        val existing = ref?.get()
        if (existing != null) return existing

        val newProvider = Provider(
            name = name?.let { "$it($arg)" },
            createFn = { ref -> builder(ref, arg) }
        )
        cachedProviders[arg] = WeakReference(newProvider)
        return newProvider
    }
}

/**
 * Extensions to easily create families out of provider builders.
 */
fun <Arg, T> Provider.Companion.family(
    name: String? = null,
    builder: (ProviderRef, Arg) -> T
): ProviderFamily<Arg, T> {
    return ProviderFamily(name, builder)
}

/**
 * A provider builder that takes an argument to create parameterized future providers.
 */
class FutureProviderFamily<Arg, T>(
    private val name: String? = null,
    private val builder: suspend (ProviderRef, Arg) -> T
) {
    private val cachedProviders = ConcurrentHashMap<Arg, WeakReference<FutureProvider<T>>>()

    operator fun invoke(arg: Arg): FutureProvider<T> {
        val ref = cachedProviders[arg]
        val existing = ref?.get()
        if (existing != null) return existing

        val newProvider = FutureProvider(
            name = name?.let { "$it($arg)" },
            createFn = { ref -> builder(ref, arg) }
        )
        cachedProviders[arg] = WeakReference(newProvider)
        return newProvider
    }
}

/**
 * Extensions to easily create families out of FutureProvider builders.
 */
fun <Arg, T> FutureProvider.Companion.family(
    name: String? = null,
    builder: suspend (ProviderRef, Arg) -> T
): FutureProviderFamily<Arg, T> {
    return FutureProviderFamily(name, builder)
}
