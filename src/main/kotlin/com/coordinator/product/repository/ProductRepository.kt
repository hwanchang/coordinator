package com.coordinator.product.repository

import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import java.math.BigDecimal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    override fun findAll(): List<Product>
}
