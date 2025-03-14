package com.coordinator.configuration

import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Component

@Component
class InMemoryCache<String, V>(
    private val store: MutableMap<String, V> = ConcurrentHashMap<String, V>(),
) : Cache<String, V> {
    override fun put(key: String, value: V) {
        store[key] = value
    }

    override fun getOrNull(key: String): V? {
        return store[key]
    }

    override fun remove(key: String) {
        store.remove(key)
    }
}
