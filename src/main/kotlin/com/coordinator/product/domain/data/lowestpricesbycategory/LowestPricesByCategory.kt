package com.coordinator.product.domain.data.lowestpricesbycategory

import java.math.BigDecimal

data class LowestPricesByCategory(
    val lowestPriceProducts: List<LowestPrices>,
) {
    val totalPrice: BigDecimal = lowestPriceProducts.sumOf(LowestPrices::price)
}
