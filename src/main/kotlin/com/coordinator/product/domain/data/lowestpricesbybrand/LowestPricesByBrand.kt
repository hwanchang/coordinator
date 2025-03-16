package com.coordinator.product.domain.data.lowestpricesbybrand

import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import java.math.BigDecimal

data class LowestPricesByBrand(
    val brandName: String,

    val products: List<Product>,
) {
    val totalPrice: BigDecimal = products.sumOf(Product::price)

    init {
        require(products.size == Category.entries.size) { "상품이 각 카테고리별로 한 개씩 필요합니다." }
    }
}
