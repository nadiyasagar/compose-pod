package com.brine.composepod.core

/**
 * Interface marking a provider that should have its state destroyed
 * when no longer being listened to by the UI or other providers.
 */
interface AutoDisposeProvider<T> {
    val origin: ProviderBase<T>
}

/**
 * Creates an auto-dispose wrapper for any ProviderBase.
 */
fun <T> ProviderBase<T>.autoDispose(): ProviderBase<T> {
    val origin = this
    return object : ProviderBase<T>(origin.name?.let { "$it-autoDispose" }), AutoDisposeProvider<T> {
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
    // Cache the uniquely generated providers based on argument
    private val cachedProviders = mutableMapOf<Arg, Provider<T>>()

    operator fun invoke(arg: Arg): Provider<T> {
        return cachedProviders.getOrPut(arg) {
            Provider(
                name = name?.let { "$it($arg)" },
                createFn = { ref -> builder(ref, arg) }
            )
        }
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
    private val cachedProviders = mutableMapOf<Arg, FutureProvider<T>>()

    operator fun invoke(arg: Arg): FutureProvider<T> {
        return cachedProviders.getOrPut(arg) {
            FutureProvider(
                name = name?.let { "$it($arg)" },
                createFn = { ref -> builder(ref, arg) }
            )
        }
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
