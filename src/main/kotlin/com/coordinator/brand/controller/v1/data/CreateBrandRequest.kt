package com.coordinator.brand.controller.v1.data

import com.coordinator.brand.domain.Brand

class CreateBrandRequest(
    val name: String,
) {
    fun toDomain() = Brand(name = name)
}
