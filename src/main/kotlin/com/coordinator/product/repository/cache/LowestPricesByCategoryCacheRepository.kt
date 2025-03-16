package com.coordinator.product.repository.cache

import com.coordinator.configuration.cache.Cache
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices
import org.springframework.stereotype.Repository

@Repository
class LowestPricesByCategoryCacheRepository(
    private val cache: Cache<String, LowestPrices>,
) : CacheRepository<String, LowestPrices> {
    override fun save(key: String, value: LowestPrices) {
        cache.put(key, value)
    }

    override fun getOrNull(key: String): LowestPrices? {
        return cache.getOrNull(key)
    }

    override fun remove(key: String) {
        cache.remove(key)
    }
}
