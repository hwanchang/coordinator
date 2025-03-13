package com.coordinator.product.controller.v1.data

import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import java.math.BigDecimal
import java.time.LocalDateTime

class ProductResponse(product: Product) {
    val id: Long = product.id

    val brandId: Long = product.brandId

    val category: Category = product.category

    val price: BigDecimal = product.price

    val createdAt: LocalDateTime = product.createdAt

    val updatedAt: LocalDateTime? = product.updatedAt
}
