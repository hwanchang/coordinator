package com.coordinator.product.service

import com.coordinator.brand.domain.Brand
import com.coordinator.brand.service.BrandService
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrice
import com.coordinator.product.repository.cache.LowestPricesByBrandCacheRepository
import com.coordinator.product.repository.cache.LowestPricesByCategoryCacheRepository
import com.coordinator.product.repository.cache.MinMaxPricesByCategoryCacheRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ProductCache(
    private val brandService: BrandService,

    private val lowestPricesByCategoryCacheRepository: LowestPricesByCategoryCacheRepository,

    @Value("\${cache.key.lowest-prices-by-category}")
    private val lowestPricesByCategoryKey: String,

    private val lowestPricesByBrandCacheRepository: LowestPricesByBrandCacheRepository,

    @Value("\${cache.key.lowest-prices-by-brand}")
    private val lowestPricesByBrandKey: String,

    private val minMaxPricesByCategoryCacheRepository: MinMaxPricesByCategoryCacheRepository,

    @Value("\${cache.key.min-max-prices-by-category}")
    private val minMaxPricesByCategoryKey: String,
) {
    fun updateCache(product: Product) {
        val brand = brandService.getBrand(brandId = product.brandId)

        updateLowestPriceByCategoryCache(brand = brand, product = product)
        updateLowestPriceByBrandCache(brand = brand, product = product)
        updateMinMaxPriceByCategoryCache(product = product)
    }

    private fun updateLowestPriceByCategoryCache(brand: Brand, product: Product) {
        val category = product.category
        val lowestPrices = getLowestPriceByCategoryCache(category) ?: return

        val brandName = brand.name
        if (product.price == lowestPrices.price) {
            saveLowestPriceByCategoryCache(
                lowestPrices = LowestPrices(
                    category = category,
                    brandNames = lowestPrices.brandNames + brandName,
                    price = lowestPrices.price,
                ),
            )
        } else if (product.price < lowestPrices.price) {
            saveLowestPriceByCategoryCache(
                lowestPrices = LowestPrices(
                    category = category,
                    brandNames = setOf(brandName),
                    price = product.price,
                ),
            )
        } else {
            val key = "$lowestPricesByCategoryKey:$category"

            lowestPricesByCategoryCacheRepository.remove(key = key)
        }
    }

    private fun updateLowestPriceByBrandCache(brand: Brand, product: Product) {
        val brandName = brand.name
        val lowestPricesByBrand = getLowestPriceByBrandCache(brandName) ?: return

        val cachedProduct = lowestPricesByBrand.products.firstOrNull { it.category == product.category }
            ?: throw IllegalStateException("상품은 최소 1개는 존재해야합니다.")

        if (product.price <= cachedProduct.price) {
            val newProducts = lowestPricesByBrand.products.map { if (it.category == product.category) product else it }
            saveLowestPriceByBrandCache(
                lowestPricesByBrand = LowestPricesByBrand(brandName = brandName, products = newProducts),
            )
        } else {
            val key = "$lowestPricesByBrandKey:$brandName"

            lowestPricesByBrandCacheRepository.remove(key = key)
        }
    }

    private fun updateMinMaxPriceByCategoryCache(product: Product) {
        val category = product.category
        val (minPrice, maxPrice) = getMinMaxPriceByCategoryCache(category) ?: return

        if (product.price < minPrice) {
            saveMinMaxPriceByCategoryCache(
                category = category,
                minMaxPrice = MinMaxPrice(minPrice = product.price, maxPrice = maxPrice),
            )
        }

        if (product.price > maxPrice) {
            saveMinMaxPriceByCategoryCache(
                category = category,
                minMaxPrice = MinMaxPrice(minPrice = minPrice, maxPrice = product.price),
            )
        }
    }

    fun deleteCache(product: Product) {
        val brand = brandService.getBrand(brandId = product.brandId)

        deleteLowestPriceByCategoryCache(brand = brand, product = product)
        deleteLowestPriceByBrandCache(brand = brand)
        deleteMinMaxPriceByCategoryCache(product = product)
    }

    private fun deleteLowestPriceByCategoryCache(brand: Brand, product: Product) {
        val category = product.category
        val lowestPrices = getLowestPriceByCategoryCache(category) ?: return

        val brandNames = lowestPrices.brandNames
        if (brandNames.contains(brand.name)) {
            val key = "$lowestPricesByCategoryKey:$category"

            lowestPricesByCategoryCacheRepository.remove(key = key)
        }
    }


    private fun deleteLowestPriceByBrandCache(brand: Brand) {
        val brandName = brand.name
        val lowestPricesByBrand = getLowestPriceByBrandCache(brandName) ?: return

        if (lowestPricesByBrand.brandName == brandName) {
            val key = "$lowestPricesByBrandKey:$brandName"

            lowestPricesByBrandCacheRepository.remove(key = key)
        }
    }


    private fun deleteMinMaxPriceByCategoryCache(product: Product) {
        val category = product.category
        val (minPrice, maxPrice) = getMinMaxPriceByCategoryCache(category) ?: return

        if (minPrice == product.price || maxPrice == product.price) {
            val key = "$minMaxPricesByCategoryKey:$category"

            minMaxPricesByCategoryCacheRepository.remove(key = key)
        }
    }

    fun saveLowestPriceByCategoryCache(lowestPrices: LowestPrices) {
        val key = "$lowestPricesByCategoryKey:${lowestPrices.category}"

        lowestPricesByCategoryCacheRepository.save(key = key, value = lowestPrices)
    }

    fun saveLowestPriceByBrandCache(lowestPricesByBrand: LowestPricesByBrand) {
        val key = "$lowestPricesByBrandKey:${lowestPricesByBrand.brandName}"

        lowestPricesByBrandCacheRepository.save(key = key, value = lowestPricesByBrand)
    }

    fun saveMinMaxPriceByCategoryCache(category: Category, minMaxPrice: MinMaxPrice) {
        val key = "$minMaxPricesByCategoryKey:$category"

        minMaxPricesByCategoryCacheRepository.save(key = key, value = minMaxPrice)
    }

    fun getLowestPriceByCategoryCache(category: Category): LowestPrices? {
        val key = "$lowestPricesByCategoryKey:$category"

        return lowestPricesByCategoryCacheRepository.getOrNull(key = key)
    }

    fun getLowestPriceByBrandCache(brandName: String): LowestPricesByBrand? {
        val key = "$lowestPricesByBrandKey:$brandName"

        return lowestPricesByBrandCacheRepository.getOrNull(key = key)
    }

    fun getMinMaxPriceByCategoryCache(category: Category): MinMaxPrice? {
        val key = "$minMaxPricesByCategoryKey:$category"

        return minMaxPricesByCategoryCacheRepository.getOrNull(key = key)
    }
}
