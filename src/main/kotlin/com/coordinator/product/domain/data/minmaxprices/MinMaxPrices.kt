package com.coordinator.product.domain.data.minmaxprices

import com.coordinator.product.domain.Category
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices

data class MinMaxPrices(
    val category: Category,

    val minPrice: LowestPrices,

    val maxPrice: LowestPrices,
)
