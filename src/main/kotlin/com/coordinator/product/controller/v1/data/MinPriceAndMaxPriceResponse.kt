package com.coordinator.product.controller.v1.data

import com.coordinator.common.serializer.BigDecimalPriceSerializer
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrices
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.math.BigDecimal

data class MinPriceAndMaxPriceResponse(
    @JsonProperty("카테고리")
    val category: Category,

    @JsonProperty("최저가")
    val minPricePrices: List<PricePriceResponse>,

    @JsonProperty("최고가")
    val maxPricePrices: List<PricePriceResponse>,
) {
    data class PricePriceResponse(
        @JsonProperty("브랜드")
        val brandName: String,

        @JsonProperty("가격")
        @JsonSerialize(using = BigDecimalPriceSerializer::class)
        val price: BigDecimal,
    )

    companion object {
        fun from(minMaxPrices: MinMaxPrices): MinPriceAndMaxPriceResponse {
            val minPricePrice = minMaxPrices.minPrice
            val minBrandNames = minPricePrice.brandNames

            val maxPricePrice = minMaxPrices.maxPrice
            val maxBrandNames = maxPricePrice.brandNames

            return MinPriceAndMaxPriceResponse(
                category = minMaxPrices.category,
                minPricePrices = minBrandNames.map { PricePriceResponse(brandName = it, price = minPricePrice.price) },
                maxPricePrices = maxBrandNames.map { PricePriceResponse(brandName = it, price = maxPricePrice.price) },
            )
        }
    }
}
