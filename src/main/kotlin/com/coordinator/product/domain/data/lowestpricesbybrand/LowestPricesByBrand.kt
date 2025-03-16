package com.coordinator.product.domain.data.lowestpricesbybrand

import com.coordinator.product.domain.Product
import java.math.BigDecimal

data class LowestPricesByBrand(
    val brandName: String,

    val products: List<Product>,
) {
    val totalPrice: BigDecimal = products.sumOf(Product::price)
}
