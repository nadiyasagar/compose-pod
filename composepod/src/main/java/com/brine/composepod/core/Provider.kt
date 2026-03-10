package com.brine.composepod.core

/**
 * A provider that exposes a read-only value.
 * Used for dependency injection and simple values.
 */
class Provider<T>(
    name: String? = null,
    private val createFn: (ProviderRef) -> T
) : ProviderBase<T>(name) {
    override fun create(ref: ProviderRef): T = createFn(ref)
}

/**
 * Creates a [Provider].
 */
fun <T> provider(name: String? = null, create: (ProviderRef) -> T): Provider<T> {
    return Provider(name, create)
}
