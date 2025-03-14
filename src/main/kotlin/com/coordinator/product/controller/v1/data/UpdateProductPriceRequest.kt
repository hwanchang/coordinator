package com.coordinator.product.controller.v1.data

import java.math.BigDecimal

data class UpdateProductPriceRequest(
    val price: BigDecimal,
)
