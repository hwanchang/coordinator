package com.coordinator.product.service

import com.coordinator.brand.service.BrandService
import com.coordinator.product.domain.Product
import com.coordinator.product.repository.ProductRepository
import jakarta.persistence.EntityNotFoundException
import java.math.BigDecimal
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val brandService: BrandService,

    private val productRepository: ProductRepository,
) {
    @Transactional
    fun createProduct(product: Product) {
        require(brandService.existsById(brandId = product.brandId)) { "${product.brandId}: 해당 브랜드를 찾을 수 없습니다." }

        productRepository.save(product)
    }

    @Transactional(readOnly = true)
    fun getProducts(): List<Product> = productRepository.findAll()

    @Transactional(readOnly = true)
    fun getProduct(productId: Long): Product = productRepository.findByIdOrNull(id = productId)
        ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")

    @Transactional
    fun updateProduct(productId: Long, price: BigDecimal) {
        val product = productRepository.findByIdOrNull(id = productId)
            ?.apply { update(price = price) }
            ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")

        productRepository.save(product)
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        productRepository.findByIdOrNull(id = productId)
            ?.also(productRepository::delete)
            ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")
    }
}
