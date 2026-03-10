package com.brine.composepod.core

/**
 * The base class for all providers.
 * @param name An optional name for debugging purposes.
 */
abstract class ProviderBase<T>(val name: String? = null) {
    /**
     * Creates the initial state or value for this provider.
     */
    abstract fun create(ref: ProviderRef): T
}
