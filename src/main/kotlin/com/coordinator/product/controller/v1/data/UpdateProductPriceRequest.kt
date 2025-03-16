package com.coordinator.product.controller.v1.data

import com.coordinator.common.serializer.BigDecimalPriceDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.math.BigDecimal

data class UpdateProductPriceRequest(
    @JsonDeserialize(using = BigDecimalPriceDeserializer::class)
    val price: BigDecimal,
)
