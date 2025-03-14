package com.coordinator.product.repository.cache

import com.coordinator.configuration.Cache
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices
import org.springframework.stereotype.Repository

@Repository
class LowestPricesByCategoryCacheRepository(
    private val cache: Cache<String, LowestPrices>,
) {
    fun save(key: String, value: LowestPrices) {
        cache.put(key, value)
    }

    fun getOrNull(key: String): LowestPrices? {
        return cache.getOrNull(key)
    }

    fun remove(key: String) {
        cache.remove(key)
    }
}
