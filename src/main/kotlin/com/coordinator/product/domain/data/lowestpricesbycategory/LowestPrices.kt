package com.coordinator.product.domain.data.lowestpricesbycategory

import com.coordinator.product.domain.Category
import java.math.BigDecimal

data class LowestPrices(
    val category: Category,

    val brandNames: List<String>,

    val price: BigDecimal,
) {
    init {
        require(brandNames.isNotEmpty()) { "브랜드가 존재해야 합니다." }
    }
}
