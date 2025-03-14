package com.coordinator.product.controller.v1

import com.coordinator.common.api.ApiResponse
import com.coordinator.common.api.ApiResponse.Success
import com.coordinator.product.controller.v1.data.CreateProductRequest
import com.coordinator.product.controller.v1.data.LowestPricesByBrandResponse
import com.coordinator.product.controller.v1.data.LowestPricesByCategoryResponse
import com.coordinator.product.controller.v1.data.MinPriceAndMaxPriceResponse
import com.coordinator.product.controller.v1.data.ProductResponse
import com.coordinator.product.controller.v1.data.UpdateProductPriceRequest
import com.coordinator.product.domain.Category
import com.coordinator.product.service.ProductService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("api/v1/products")
@RestController
class ProductRestController(
    private val productService: ProductService,
) {
    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequest): ApiResponse<String> {
        productService.createProduct(product = request.toDomain())

        return Success(result = "요청 성공")
    }

    @GetMapping
    fun getProducts(): ApiResponse<List<ProductResponse>> =
        productService.getProducts().map(::ProductResponse).let { Success(result = it) }

    @GetMapping("{productId}")
    fun getProduct(@PathVariable productId: Long): ApiResponse<ProductResponse> {
        val product = productService.getProduct(productId = productId)

        return Success(result = ProductResponse(product = product))
    }

    @PatchMapping("{productId}")
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: UpdateProductPriceRequest,
    ): ApiResponse<String> {
        productService.updateProduct(productId = productId, price = request.price)

        return Success(result = "요청 성공")
    }

    @DeleteMapping("{productId}")
    fun deleteProduct(@PathVariable productId: Long): ApiResponse<String> {
        productService.deleteProduct(productId = productId)

        return Success(result = "요청 성공")
    }

    @GetMapping("lowest-price-by-categories")
    fun getLowestPricesByCategory(): ApiResponse<LowestPricesByCategoryResponse> {
        val lowestPricesByCategory = productService.getLowestPricesByCategory()

        return Success(result = LowestPricesByCategoryResponse(lowestPricesByCategory = lowestPricesByCategory))
    }

    @GetMapping("lowest-price-by-brands")
    fun getLowestPricesByBrand(): ApiResponse<LowestPricesByBrandResponse> {
        val lowestPricesByBrand = productService.getLowestPricesByBrand()

        return Success(result = LowestPricesByBrandResponse(lowestPricesByBrand = lowestPricesByBrand))
    }

    @GetMapping("min-max-prices")
    fun getMinPriceAndMaxPriceByCategory(@RequestParam category: Category): ApiResponse<MinPriceAndMaxPriceResponse> {
        val minMaxPrices = productService.getMinPriceAndMaxPriceByCategory(category = category)

        return Success(result = MinPriceAndMaxPriceResponse(minMaxPrices = minMaxPrices))
    }
}
