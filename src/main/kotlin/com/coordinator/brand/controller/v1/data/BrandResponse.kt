package com.coordinator.brand.controller.v1.data

import com.coordinator.brand.domain.Brand
import java.time.LocalDateTime

class BrandResponse(brand: Brand) {
    val id: Long = brand.id

    val name: String = brand.name

    val createdAt: LocalDateTime = brand.createdAt

    val updatedAt: LocalDateTime? = brand.updatedAt
}
