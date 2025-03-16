package com.coordinator.brand.controller.v1

import com.coordinator.brand.controller.v1.data.BrandResponse
import com.coordinator.brand.controller.v1.data.CreateBrandRequest
import com.coordinator.brand.controller.v1.data.UpdateBrandNameRequest
import com.coordinator.brand.service.BrandService
import com.coordinator.common.api.ApiResponse
import com.coordinator.common.api.ApiResponse.Success
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
    fun createBrand(@RequestBody request: CreateBrandRequest): ApiResponse<BrandResponse> {
        val response = brandService.createBrand(brand = request.toDomain()).let(BrandResponse::from)

        return Success(result = response)
    }

    @GetMapping
    fun getBrands(): ApiResponse<List<BrandResponse>> =
        brandService.getBrands().map(BrandResponse::from).let { Success(result = it) }

    @GetMapping("{brandId}")
    fun getBrand(@PathVariable brandId: Long): ApiResponse<BrandResponse> {
        val response = brandService.getBrand(brandId = brandId).let(BrandResponse::from)

        return Success(result = response)
    }

    @PatchMapping("{brandId}")
    fun updateBrand(
        @PathVariable brandId: Long,
        @RequestBody request: UpdateBrandNameRequest,
    ): ApiResponse<BrandResponse> {
        val response = brandService.updateBrand(brandId = brandId, name = request.name).let(BrandResponse::from)

        return Success(result = response)
    }

    @DeleteMapping("{brandId}")
    @ResponseStatus(NO_CONTENT)
    fun deleteBrand(@PathVariable brandId: Long) {
        brandService.deleteBrand(brandId = brandId)
    }
}
