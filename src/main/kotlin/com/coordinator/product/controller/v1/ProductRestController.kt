package com.coordinator.product.controller.v1

import com.coordinator.product.controller.v1.data.CreateProductRequest
import com.coordinator.product.controller.v1.data.LowestPricesByCategoryResponse
import com.coordinator.product.controller.v1.data.ProductResponse
import com.coordinator.product.controller.v1.data.UpdateProductPriceRequest
import com.coordinator.product.service.ProductService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RequestMapping("api/v1/products")
@RestController
class ProductRestController(
    private val productService: ProductService,
) {
    @PostMapping
    @ResponseStatus(CREATED)
    fun createProduct(@RequestBody request: CreateProductRequest) {
        productService.createProduct(product = request.toDomain())
    }

    @GetMapping
    fun getProducts(): List<ProductResponse> = productService.getProducts().map(::ProductResponse)

    @GetMapping("{productId}")
    fun getProduct(@PathVariable productId: Long): ProductResponse =
        productService.getProduct(productId = productId).let(::ProductResponse)

    @PatchMapping("{productId}")
    @ResponseStatus(NO_CONTENT)
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: UpdateProductPriceRequest,
    ) {
        productService.updateProduct(productId = productId, price = request.price)
    }

    @DeleteMapping("{productId}")
    @ResponseStatus(NO_CONTENT)
    fun deleteProduct(@PathVariable productId: Long) {
        productService.deleteProduct(productId = productId)
    }

    @GetMapping("lowest-price-by-categories")
    fun getLowestPricesByCategory(): LowestPricesByCategoryResponse =
        productService.getLowestPricesByCategory().let(::LowestPricesByCategoryResponse)
}
