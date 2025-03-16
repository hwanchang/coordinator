package com.coordinator.product.service

import com.coordinator.brand.domain.Brand
import com.coordinator.brand.service.BrandService
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Product
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPricesByCategory
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrices
import com.coordinator.product.repository.jpa.ProductRepository
import jakarta.persistence.EntityNotFoundException
import java.math.BigDecimal
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val brandService: BrandService,

    private val productRepository: ProductRepository,

    private val productCache: ProductCache,
) {
    @Transactional
    fun createProduct(product: Product): Product {
        require(brandService.existsById(brandId = product.brandId)) { "${product.brandId}: 해당 브랜드를 찾을 수 없습니다." }

        return productRepository.save(product)
            .also { productCache.updateCache(product) }
    }

    @Transactional(readOnly = true)
    fun getProducts(): List<Product> = productRepository.findAll()

    @Transactional(readOnly = true)
    fun getProduct(productId: Long): Product = productRepository.findByIdOrNull(id = productId)
        ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")

    @Transactional
    fun updateProduct(productId: Long, price: BigDecimal): Product {
        val product = productRepository.findByIdOrNull(id = productId)
            ?.apply { update(price = price) }
            ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")

        return productRepository.save(product)
            .also { productCache.updateCache(product) }
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        val product = productRepository.findByIdOrNull(id = productId)
            ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")

        val productCount = productRepository.countByBrandIdAndCategory(product.brandId, product.category)
        check(productCount > 1) { "상품이 한 개인 경우는 삭제할 수 없습니다." } // 상품 품절은 없다고 가정

        productRepository.delete(product)
            .also { productCache.deleteCache(product) }
    }

    @Transactional(readOnly = true)
    fun getLowestPricesByCategory(): LowestPricesByCategory {
        return Category.entries.map { category ->
            productCache.getLowestPriceByCategoryCache(category = category)
                ?: getLowestPricesByCategory(category = category) // 캐시 조회 후 없는 경우 db 조회
        }.let(::LowestPricesByCategory)
    }

    private fun getLowestPricesByCategory(category: Category): LowestPrices {
        val minPrice = productRepository.findMinPriceByCategory(category = category)
            ?: throw EntityNotFoundException("category - $category: 해당 상품을 찾을 수 없습니다.")
        val products = productRepository.findAllByCategoryAndPrice(category = category, price = minPrice)

        check(products.isNotEmpty()) { "상품은 최소 1개는 존재해야합니다." }

        val brandIds = products.map(Product::brandId).distinct()
        val brandNames = brandService.getAllBrandsByIds(brandIds = brandIds).map(Brand::name).toSet()

        return LowestPrices(brandNames = brandNames, category = category, price = minPrice)
            .also(productCache::saveLowestPriceByCategoryCache) // 카테고리별 최저가 db 조회 후 캐시에 저장
    }

    @Transactional(readOnly = true)
    fun getLowestPricesByBrand(): LowestPricesByBrand {
        return brandService.getBrands().mapNotNull { brand ->
            productCache.getLowestPriceByBrandCache(brandName = brand.name)
                ?: getLowestPricesByBrand(brand = brand) // 캐시 조회 후 없는 경우 db 조회
        }.minBy(LowestPricesByBrand::totalPrice)
    }

    private fun getLowestPricesByBrand(brand: Brand): LowestPricesByBrand? {
        val products = Category.entries.mapNotNull { category ->
            /*
            같은 브랜드에 동일 최저가 상품이 있어도 가격만 활용하여 최저가 비교이기 때문에 동일 최저가를 List 로 받지 않고 1개만 조회하여 가격 활용
            맨 첫 번째 데이터 한 건만 조회하기 때문에 성능에 큰 문제가 없어서 이후 가격 외 다른 데이터를 활용할 경우도 대비하여 Entity 를 조회하도록 활용하지만
            더 효율적 조회를 해야할 정도의 상황이라면 Product 엔티티를 조회하는게 아닌 MIN(price) 조회를 활용해서 필요한 가격만 조회하도록 네트워크 비용 감소 가능
            */
            productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(brandId = brand.id, category = category)
        }

        // 모든 카테고리에 대한 상품이 다 등록되기 전까진 브랜드별 총액 최저가 조회에서 제외시키도록 null 을 return
        if (products.size != Category.entries.size) {
            return null
        }

        return LowestPricesByBrand(brandName = brand.name, products = products)
            .also(productCache::saveLowestPriceByBrandCache) // 브랜드별 최저가 db 조회 후 캐시에 저장
    }

    @Transactional(readOnly = true)
    fun getMinPriceAndMaxPriceByCategory(category: Category): MinMaxPrices {
        val (minPrice, maxPrice) = productCache.getMinMaxPriceByCategoryCache(category = category)
        /*
        동일 최저가 혹은 동일 최고가가 있다면 응답에 List 로 전달해야하기 때문에 최저가, 최고가를 미리 조회하여 해당 가격으로 데이터 조회 필요
        최저가, 최고가 조회에는 MIN(price), MAX(price) 를 활용하여 한 번의 쿼리로 보내는게 가장 효율적이기 때문에 해당 쿼리 적용
        */
            ?: productRepository.findMinMaxPriceByCategory(category = category)
                // 카테고리별 최저가, 최고가 db 조회 후 캐시에 저장
                .also { minMaxPrice ->
                    productCache.saveMinMaxPriceByCategoryCache(category = category, minMaxPrice = minMaxPrice)
                }

        return MinMaxPrices(
            category = category,
            minPrice = getPriceByCategory(category = category, price = minPrice),
            maxPrice = getPriceByCategory(category = category, price = maxPrice),
        )
    }

    private fun getPriceByCategory(category: Category, price: BigDecimal): LowestPrices {
        val brandIds = productRepository.findAllByCategoryAndPrice(category = category, price = price)
            .map(Product::brandId)
        val brandNames = brandService.getAllBrandsByIds(brandIds = brandIds)
            .map(Brand::name).toSet()

        return LowestPrices(brandNames = brandNames, category = category, price = price)
    }
}
