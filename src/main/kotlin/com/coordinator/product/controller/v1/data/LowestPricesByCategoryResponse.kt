package com.coordinator.product.controller.v1.data

import com.coordinator.common.serializer.BigDecimalPriceSerializer
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPricesByCategory
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.math.BigDecimal

data class LowestPricesByCategoryResponse(
    @JsonProperty("카테고리")
    val products: List<LowestPriceResponse>,

    @JsonProperty("총액")
    @JsonSerialize(using = BigDecimalPriceSerializer::class)
    val totalPrice: BigDecimal,
) {
    data class LowestPriceResponse(
        @JsonProperty("카테고리")
        val category: Category,

        /*
        * 해당 카테고리에 동일한 최저가를 가진 여러 브랜드가 있을 수 있기 때문에 List 로 전달
        * 서버에서 해당 최저가의 모든 브랜드를 내려주고 Frontend 에서는 값 변경 없이 정책에 맞춰 한 개 혹은 다수의 브랜드를 자유롭게 화면에 표시 가능
        * */
        @JsonProperty("브랜드")
        val brand: Set<String>,

        @JsonProperty("가격")
        @JsonSerialize(using = BigDecimalPriceSerializer::class)
        val price: BigDecimal,
    ) {
        companion object {
            fun from(lowestPrices: LowestPrices) = LowestPriceResponse(
                category = lowestPrices.category,
                brand = lowestPrices.brandNames,
                price = lowestPrices.price,
            )
        }
    }

    companion object {
        fun from(lowestPricesByCategory: LowestPricesByCategory) = LowestPricesByCategoryResponse(
            products = lowestPricesByCategory.lowestPriceProducts.map(LowestPriceResponse::from),
            totalPrice = lowestPricesByCategory.totalPrice,
        )
    }
}
