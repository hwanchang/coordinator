package com.coordinator.product.repository.cache

import com.coordinator.configuration.Cache
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import org.springframework.stereotype.Repository

@Repository
class LowestPricesByBrandCacheRepository(
    private val cache: Cache<String, LowestPricesByBrand>,
) {
    fun save(key: String, value: LowestPricesByBrand) {
        cache.put(key, value)
    }

    fun getOrNull(key: String): LowestPricesByBrand? {
        return cache.getOrNull(key)
    }

    fun remove(key: String) {
        cache.remove(key)
    }
}
