package com.coordinator.brand.controller.v1

import com.coordinator.brand.controller.v1.data.BrandResponse
import com.coordinator.brand.controller.v1.data.CreateBrandRequest
import com.coordinator.brand.controller.v1.data.UpdateBrandNameRequest
import com.coordinator.brand.service.BrandService
import com.coordinator.common.api.ApiResponse
import com.coordinator.common.api.ApiResponse.Success
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("api/v1/brands")
@RestController
class BrandRestController(
    private val brandService: BrandService,
) {
    @PostMapping
    fun createBrand(@RequestBody request: CreateBrandRequest): ApiResponse<String> {
        brandService.createBrand(brand = request.toDomain())

        return Success(result = "요청 성공")
    }

    @GetMapping
    fun getBrands(): ApiResponse<List<BrandResponse>> =
        brandService.getBrands().map(::BrandResponse).let { Success(result = it) }

    @GetMapping("{brandId}")
    fun getBrand(@PathVariable brandId: Long): ApiResponse<BrandResponse> {
        val brand = brandService.getBrand(brandId = brandId)

        return Success(result = BrandResponse(brand = brand))
    }

    @PatchMapping("{brandId}")
    fun updateBrand(
        @PathVariable brandId: Long,
        @RequestBody request: UpdateBrandNameRequest,
    ): ApiResponse<String> {
        brandService.updateBrand(brandId = brandId, name = request.name)

        return Success(result = "요청 성공")
    }

    @DeleteMapping("{brandId}")
    fun deleteBrand(@PathVariable brandId: Long): ApiResponse<String> {
        brandService.deleteBrand(brandId = brandId)

        return Success(result = "요청 성공")
    }
}
