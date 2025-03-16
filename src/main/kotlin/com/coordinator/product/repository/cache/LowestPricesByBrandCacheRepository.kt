package com.coordinator.product.repository.cache

import com.coordinator.configuration.cache.Cache
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import org.springframework.stereotype.Repository

@Repository
class LowestPricesByBrandCacheRepository(
    private val cache: Cache<String, LowestPricesByBrand>,
) : CacheRepository<String, LowestPricesByBrand> {
    override fun save(key: String, value: LowestPricesByBrand) {
        cache.put(key, value)
    }

    override fun getOrNull(key: String): LowestPricesByBrand? {
        return cache.getOrNull(key)
    }

    override fun remove(key: String) {
        cache.remove(key)
    }
}
