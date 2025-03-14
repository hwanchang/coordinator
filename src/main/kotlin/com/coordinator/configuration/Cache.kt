package com.coordinator.configuration

interface Cache<K, V> {
    fun put(key: K, value: V)

    fun getOrNull(key: K): V?

    fun remove(key: K)
}
