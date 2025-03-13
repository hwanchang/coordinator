package com.coordinator.product.controller.v1.data

import com.coordinator.common.serializer.BigDecimalPriceSerializer
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.math.BigDecimal

class LowestPricesByBrandResponse(lowestPricesByBrand: LowestPricesByBrand) {
    @JsonProperty("최저가")
    val lowestPriceProducts: LowestPriceProductResponse =
        LowestPriceProductResponse(lowestPricesByBrand = lowestPricesByBrand)

    class LowestPriceProductResponse(lowestPricesByBrand: LowestPricesByBrand) {
        @JsonProperty("브랜드")
        val brand: String = lowestPricesByBrand.brandName

        @JsonProperty("카테고리")
        val productDetails: List<LowestPriceDetailResponse> =
            lowestPricesByBrand.products.map(::LowestPriceDetailResponse)

        @JsonProperty("총액")
        @JsonSerialize(using = BigDecimalPriceSerializer::class)
        val totalPrice: BigDecimal = lowestPricesByBrand.totalPrice

        class LowestPriceDetailResponse(product: Product) {
            @JsonProperty("카테고리")
            val category: Category = product.category

            @JsonProperty("가격")
            @JsonSerialize(using = BigDecimalPriceSerializer::class)
            val price: BigDecimal = product.price
        }
    }
}
