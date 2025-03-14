package com.coordinator.product.repository.cache

import com.coordinator.configuration.Cache
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrice
import org.springframework.stereotype.Repository

@Repository
class MinMaxPriceByCategoryCacheRepository(
    private val cache: Cache<String, MinMaxPrice>,
) {
    fun save(key: Category, value: MinMaxPrice) {
        cache.put("min-max-price-by-category:$key", value)
    }

    fun getOrNull(key: Category): MinMaxPrice? {
        return cache.getOrNull("min-max-price-by-category:$key")
    }

    fun remove(key: Category) {
        cache.remove("min-max-price-by-category:$key")
    }
}
