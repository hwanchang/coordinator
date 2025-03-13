package com.coordinator.product.controller.v1.data

import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import java.math.BigDecimal

class CreateProductRequest(
    val brandId: Long,

    val name: String,

    val category: Category,

    val price: BigDecimal,
) {
    fun toDomain() = Product(
        brandId = brandId,
        name = name,
        category = category,
        price = price,
    )
}
