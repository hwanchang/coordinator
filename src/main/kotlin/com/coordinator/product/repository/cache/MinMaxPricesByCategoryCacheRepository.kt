package com.coordinator.product.repository.cache

import com.coordinator.configuration.Cache
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrice
import org.springframework.stereotype.Repository

@Repository
class MinMaxPricesByCategoryCacheRepository(
    private val cache: Cache<String, MinMaxPrice>,
) {
    fun save(key: String, value: MinMaxPrice) {
        cache.put(key, value)
    }

    fun getOrNull(key: String): MinMaxPrice? {
        return cache.getOrNull(key)
    }

    fun remove(key: String) {
        cache.remove(key)
    }
}
