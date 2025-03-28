package com.coordinator.product.repository.jpa

import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrice
import java.math.BigDecimal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    override fun findAll(): List<Product>

    fun findAllByCategoryAndPrice(category: Category, price: BigDecimal): List<Product>

    fun findFirstByBrandIdAndCategoryOrderByPriceAsc(brandId: Long, category: Category): Product?

    fun countByBrandIdAndCategory(brandId: Long, category: Category): Int

    @Query("SELECT MIN(p.price) AS minPrice FROM Product p WHERE p.category = :category")
    fun findMinPriceByCategory(category: Category): BigDecimal?

    @Query("SELECT NEW com.coordinator.product.domain.data.minmaxprices.MinMaxPrice(MIN(p.price), MAX(p.price)) FROM Product p WHERE p.category = :category")
    fun findMinMaxPriceByCategory(category: Category): MinMaxPrice
}
