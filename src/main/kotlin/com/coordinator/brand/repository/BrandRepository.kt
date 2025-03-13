package com.coordinator.brand.repository

import com.coordinator.brand.domain.Brand
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BrandRepository : JpaRepository<Brand, Long> {
    override fun findAll(): List<Brand>

    fun existsByName(name: String): Boolean

    fun findAllByIdIn(ids: List<Long>): List<Brand>
}
