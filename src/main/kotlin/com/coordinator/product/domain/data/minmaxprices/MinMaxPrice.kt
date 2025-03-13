package com.coordinator.product.domain.data.minmaxprices

import java.math.BigDecimal

data class MinMaxPrice(
    val minPrice: BigDecimal,

    val maxPrice: BigDecimal,
)
