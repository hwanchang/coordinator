package com.coordinator.product.controller.v1.data

import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductResponse(
    val id: Long,

    val brandId: Long,

    val name: String,

    val category: Category,

    val price: BigDecimal,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(product: Product) = ProductResponse(
            id = product.id,
            brandId = product.brandId,
            name = product.name,
            category = product.category,
            price = product.price,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt,
        )
    }
}
