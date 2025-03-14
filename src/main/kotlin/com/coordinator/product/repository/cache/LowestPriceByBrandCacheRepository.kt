package com.coordinator.product.repository.cache

import com.coordinator.configuration.Cache
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import org.springframework.stereotype.Repository

@Repository
class LowestPriceByBrandCacheRepository(
    private val cache: Cache<String, LowestPricesByBrand>,
) {
    fun save(key: String, value: LowestPricesByBrand) {
        cache.put("lowest-price-by-brand:$key", value)
    }

    fun getOrNull(key: String): LowestPricesByBrand? {
        return cache.getOrNull("lowest-price-by-brand:$key")
    }

    fun remove(key: String) {
        cache.remove("lowest-price-by-brand:$key")
    }
}
