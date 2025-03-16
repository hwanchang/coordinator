package com.coordinator.product.controller.v1.data

import com.coordinator.common.serializer.BigDecimalPriceDeserializer
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.math.BigDecimal

data class CreateProductRequest(
    val brandId: Long,

    val name: String,

    val category: Category,

    @JsonDeserialize(using = BigDecimalPriceDeserializer::class)
    val price: BigDecimal,
) {
    fun toDomain() = Product(
        brandId = brandId,
        name = name,
        category = category,
        price = price,
    )
}
