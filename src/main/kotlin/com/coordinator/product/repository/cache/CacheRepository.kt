package com.coordinator.product.repository.cache

interface CacheRepository<K, V> {
    fun save(key: K, value: V)

    fun getOrNull(key: K): V?

    fun remove(key: K)
}
