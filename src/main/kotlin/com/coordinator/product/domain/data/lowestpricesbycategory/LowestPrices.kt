package com.coordinator.product.domain.data.lowestpricesbycategory

import com.coordinator.brand.domain.Brand
import com.coordinator.product.domain.Category
import java.math.BigDecimal

data class LowestPrices(
    val brands: List<Brand>,

    val category: Category,

    val price: BigDecimal,
) {
    val brandNames: List<String>

    init {
        require(brands.isNotEmpty()) { "브랜드가 존재해야 합니다." }

        brandNames = brands.map(Brand::name)
    }
}
