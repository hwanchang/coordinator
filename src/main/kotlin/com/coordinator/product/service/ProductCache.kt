package com.coordinator.product.service

import com.coordinator.brand.domain.Brand
import com.coordinator.brand.service.BrandService
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrice
import com.coordinator.product.repository.cache.LowestPriceByBrandCacheRepository
import com.coordinator.product.repository.cache.LowestPriceByCategoryCacheRepository
import com.coordinator.product.repository.cache.MinMaxPriceByCategoryCacheRepository
import org.springframework.stereotype.Service

@Service
class ProductCache(
    private val brandService: BrandService,

    private val lowestPriceByCategoryCacheRepository: LowestPriceByCategoryCacheRepository,

    private val lowestPriceByBrandCacheRepository: LowestPriceByBrandCacheRepository,

    private val minMaxPriceByCategoryCacheRepository: MinMaxPriceByCategoryCacheRepository,
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
                    brandNames = listOf(brandName),
                    price = product.price,
                ),
            )
        }
    }

    private fun updateLowestPriceByBrandCache(brand: Brand, product: Product) {
        val brandName = brand.name
        val lowestPricesByBrand = getLowestPriceByBrandCache(brandName) ?: return

        val cachedProduct = lowestPricesByBrand.products.firstOrNull { it.category == product.category }
            ?: throw IllegalStateException("상품은 최소 1개는 존재해야합니다.")

        if (product.price < cachedProduct.price) {
            val newProducts = lowestPricesByBrand.products.map { if (it.category == product.category) product else it }
            saveLowestPriceByBrandCache(
                lowestPricesByBrand = LowestPricesByBrand(brandName = brandName, products = newProducts),
            )
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
            lowestPriceByCategoryCacheRepository.remove(key = category)
        }
    }


    private fun deleteLowestPriceByBrandCache(brand: Brand) {
        val brandName = brand.name
        val lowestPricesByBrand = getLowestPriceByBrandCache(brandName) ?: return

        if (lowestPricesByBrand.brandName == brandName) {
            lowestPriceByBrandCacheRepository.remove(key = brandName)
        }
    }


    private fun deleteMinMaxPriceByCategoryCache(product: Product) {
        val category = product.category
        val (minPrice, maxPrice) = getMinMaxPriceByCategoryCache(category) ?: return

        if (minPrice == product.price || maxPrice == product.price) {
            minMaxPriceByCategoryCacheRepository.remove(key = category)
        }
    }

    fun saveLowestPriceByCategoryCache(lowestPrices: LowestPrices) {
        lowestPriceByCategoryCacheRepository.save(key = lowestPrices.category, value = lowestPrices)
    }

    fun saveLowestPriceByBrandCache(lowestPricesByBrand: LowestPricesByBrand) {
        lowestPriceByBrandCacheRepository.save(key = lowestPricesByBrand.brandName, value = lowestPricesByBrand)
    }

    fun saveMinMaxPriceByCategoryCache(category: Category, minMaxPrice: MinMaxPrice) {
        minMaxPriceByCategoryCacheRepository.save(key = category, value = minMaxPrice)
    }

    fun getLowestPriceByCategoryCache(category: Category): LowestPrices? {
        return lowestPriceByCategoryCacheRepository.getOrNull(key = category)
    }

    fun getLowestPriceByBrandCache(brandName: String): LowestPricesByBrand? {
        return lowestPriceByBrandCacheRepository.getOrNull(key = brandName)
    }

    fun getMinMaxPriceByCategoryCache(category: Category): MinMaxPrice? {
        return minMaxPriceByCategoryCacheRepository.getOrNull(key = category)
    }
}
