package com.coordinator.brand.service

import com.coordinator.brand.domain.Brand
import com.coordinator.brand.repository.BrandRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BrandService(
    private val brandRepository: BrandRepository,
) {
    @Transactional
    fun createBrand(brand: Brand) {
        // 브랜드에 name 데이터만 존재하기 때문에 name 필드에 unique 적용
        check(!brandRepository.existsByName(brand.name)) { "${brand.name}: 동일한 브랜드 명이 존재합니다." }

        brandRepository.save(brand)
    }

    @Transactional(readOnly = true)
    fun getBrands(): List<Brand> = brandRepository.findAll()

    @Transactional(readOnly = true)
    fun getBrand(brandId: Long): Brand = brandRepository.findByIdOrNull(id = brandId)
        ?: throw EntityNotFoundException("$brandId: 해당 브랜드를 찾을 수 없습니다.")

    @Transactional
    fun updateBrand(brandId: Long, name: String) {
        val brand = brandRepository.findByIdOrNull(id = brandId)
            ?.apply { update(name = name) }
            ?: throw EntityNotFoundException("$brandId: 해당 브랜드를 찾을 수 없습니다.")

        brandRepository.save(brand)
    }

    @Transactional
    fun deleteBrand(brandId: Long) {
        brandRepository.findByIdOrNull(id = brandId)
            ?.also(brandRepository::delete)
            ?: throw EntityNotFoundException("$brandId: 해당 브랜드를 찾을 수 없습니다.")
    }

    fun existsById(brandId: Long) = brandRepository.existsById(brandId)

    fun getAllBrandsByIds(brandIds: List<Long>) = brandRepository.findAllByIdIn(brandIds)
}
