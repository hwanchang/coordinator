package com.coordinator.brand.controller.v1

import com.coordinator.brand.controller.v1.data.BrandResponse
import com.coordinator.brand.controller.v1.data.CreateBrandRequest
import com.coordinator.brand.controller.v1.data.UpdateBrandNameRequest
import com.coordinator.brand.service.BrandService
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

@RequestMapping("api/v1/brands")
@RestController
class BrandRestController(
    private val brandService: BrandService,
) {
    @PostMapping
    @ResponseStatus(CREATED)
    fun createBrand(@RequestBody request: CreateBrandRequest) {
        brandService.createBrand(brand = request.toDomain())
    }

    @GetMapping
    fun getBrands(): List<BrandResponse> = brandService.getBrands().map(::BrandResponse)

    @GetMapping("{brandId}")
    fun getBrand(@PathVariable brandId: Long): BrandResponse =
        brandService.getBrand(brandId = brandId).let(::BrandResponse)

    @PatchMapping("{brandId}")
    @ResponseStatus(NO_CONTENT)
    fun updateBrand(
        @PathVariable brandId: Long,
        @RequestBody request: UpdateBrandNameRequest,
    ) {
        brandService.updateBrand(brandId = brandId, name = request.name)
    }

    @DeleteMapping("{brandId}")
    @ResponseStatus(NO_CONTENT)
    fun deleteBrand(@PathVariable brandId: Long) {
        brandService.deleteBrand(brandId = brandId)
    }
}
