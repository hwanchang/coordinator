package com.coordinator.brand.controller.v1.data

import com.coordinator.brand.domain.Brand
import java.time.LocalDateTime

data class BrandResponse(
    val id: Long,

    val name: String,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(brand: Brand) = BrandResponse(
            id = brand.id,
            name = brand.name,
            createdAt = brand.createdAt,
            updatedAt = brand.updatedAt,
        )
    }
}
