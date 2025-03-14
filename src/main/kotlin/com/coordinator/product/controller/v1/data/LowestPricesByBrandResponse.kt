package com.coordinator.product.controller.v1.data

import com.coordinator.common.serializer.BigDecimalPriceSerializer
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.math.BigDecimal

data class LowestPricesByBrandResponse(
    @JsonProperty("최저가")
    val lowestPriceProducts: LowestPriceProductResponse,
) {
    data class LowestPriceProductResponse(
        @JsonProperty("브랜드")
        val brand: String,

        @JsonProperty("카테고리")
        val productDetails: List<LowestPriceDetailResponse>,

        @JsonProperty("총액")
        @JsonSerialize(using = BigDecimalPriceSerializer::class)
        val totalPrice: BigDecimal,
    ) {
        data class LowestPriceDetailResponse(
            @JsonProperty("카테고리")
            val category: Category,

            @JsonProperty("가격")
            @JsonSerialize(using = BigDecimalPriceSerializer::class)
            val price: BigDecimal,
        ) {
            companion object {
                fun from(product: Product) = LowestPriceDetailResponse(
                    category = product.category,
                    price = product.price,
                )
            }
        }

        companion object {
            fun from(lowestPricesByBrand: LowestPricesByBrand) = LowestPriceProductResponse(
                brand = lowestPricesByBrand.brandName,
                productDetails = lowestPricesByBrand.products.map(LowestPriceDetailResponse::from),
                totalPrice = lowestPricesByBrand.totalPrice,
            )
        }
    }

    companion object {
        fun from(lowestPricesByBrand: LowestPricesByBrand) = LowestPricesByBrandResponse(
            lowestPriceProducts = LowestPriceProductResponse.from(lowestPricesByBrand = lowestPricesByBrand),
        )
    }
}
