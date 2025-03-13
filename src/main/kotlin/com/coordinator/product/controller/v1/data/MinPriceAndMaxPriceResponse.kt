package com.coordinator.product.controller.v1.data

import com.coordinator.common.serializer.BigDecimalPriceSerializer
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrices
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.math.BigDecimal

class MinPriceAndMaxPriceResponse(minMaxPrices: MinMaxPrices) {
    @JsonProperty("카테고리")
    val category: Category = minMaxPrices.category

    @JsonProperty("최저가")
    val minPricePrices: List<PricePriceResponse>

    @JsonProperty("최고가")
    val maxPricePrices: List<PricePriceResponse>

    init {
        val minPricePrice = minMaxPrices.minPrice
        val minBrands = minPricePrice.brands

        val maxPricePrice = minMaxPrices.maxPrice
        val maxBrands = maxPricePrice.brands

        minPricePrices = minBrands.map { PricePriceResponse(brandName = it.name, price = minPricePrice.price) }
        maxPricePrices = maxBrands.map { PricePriceResponse(brandName = it.name, price = maxPricePrice.price) }
    }

    class PricePriceResponse(
        @JsonProperty("브랜드")
        val brandName: String,

        @JsonProperty("가격")
        @JsonSerialize(using = BigDecimalPriceSerializer::class)
        val price: BigDecimal,
    )
}
