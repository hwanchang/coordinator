package com.coordinator.product.repository.cache

import com.coordinator.configuration.Cache
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices
import org.springframework.stereotype.Repository

@Repository
class LowestPriceByCategoryCacheRepository(
    private val cache: Cache<String, LowestPrices>,
) {
    fun save(key: Category, value: LowestPrices) {
        cache.put("lowest-price-by-category:$key", value)
    }

    fun getOrNull(key: Category): LowestPrices? {
        return cache.getOrNull("lowest-price-by-category:$key")
    }

    fun remove(key: Category) {
        cache.remove("lowest-price-by-category:$key")
    }
}
