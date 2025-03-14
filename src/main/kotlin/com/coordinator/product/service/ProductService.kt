package com.coordinator.product.service

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
) {
    @Transactional
    fun createProduct(product: Product) {
        require(brandService.existsById(brandId = product.brandId)) { "${product.brandId}: 해당 브랜드를 찾을 수 없습니다." }

        productRepository.save(product)
    }

    @Transactional(readOnly = true)
    fun getProducts(): List<Product> = productRepository.findAll()

    @Transactional(readOnly = true)
    fun getProduct(productId: Long): Product = productRepository.findByIdOrNull(id = productId)
        ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")

    @Transactional
    fun updateProduct(productId: Long, price: BigDecimal) {
        val product = productRepository.findByIdOrNull(id = productId)
            ?.apply { update(price = price) }
            ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")

        productRepository.save(product)
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        val product = productRepository.findByIdOrNull(id = productId)
            ?: throw EntityNotFoundException("productId - $productId: 해당 상품을 찾을 수 없습니다.")

        val productCount = productRepository.countByBrandIdAndCategory(product.brandId, product.category)
        check(productCount > 1) { "상품이 한 개인 경우는 삭제할 수 없습니다." } // 상품 품절은 없다고 가정

        productRepository.delete(product)
    }

    @Transactional(readOnly = true)
    fun getLowestPricesByCategory(): LowestPricesByCategory {
        return Category.entries
            .map { category ->
                val minPrice = productRepository.findMinPriceByCategory(category = category)
                    ?: throw EntityNotFoundException("category - $category: 해당 상품을 찾을 수 없습니다.")
                val products = productRepository.findAllByCategoryAndPrice(category = category, price = minPrice)

                check(products.isNotEmpty()) { "상품은 최소 1개는 존재해야합니다." }

                val brandIds = products.map(Product::brandId).distinct()
                val brands = brandService.getAllBrandsByIds(brandIds)

                LowestPrices(brands = brands, category = category, price = minPrice)
            }
            .let(::LowestPricesByCategory)
    }

    @Transactional(readOnly = true)
    fun getLowestPricesByBrand(): LowestPricesByBrand {
        return brandService.getBrands().map { brand ->
            val products = Category.entries.map { category ->
                /*
                같은 브랜드에 동일 최저가 상품이 있어도 가격만 활용하여 최저가 비교이기 때문에 동일 최저가를 List 로 받지 않고 1개만 조회하여 가격 활용
                맨 첫 번째 데이터 한 건만 조회하기 때문에 성능에 큰 문제가 없어서 이후 가격 외 다른 데이터를 활용할 경우도 대비하여 Entity 를 조회하도록 활용하지만 
                더 효율적 조회를 해야할 정도의 상황이라면 Product 엔티티를 조회하는게 아닌 MIN(price) 조회를 활용해서 필요한 가격만 조회하도록 네트워크 비용 감소 가능
                */
                productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(brandId = brand.id, category = category)
                    ?: throw EntityNotFoundException("brandId - ${brand.id}, category - $category: 해당 상품을 찾을 수 없습니다.")
            }

            LowestPricesByBrand(brand = brand, products = products)
        }.minBy(LowestPricesByBrand::totalPrice)
    }

    @Transactional(readOnly = true)
    fun getMinPriceAndMaxPriceByCategory(category: Category): MinMaxPrices {
        /*
        동일 최저가 혹은 동일 최고가가 있다면 응답에 List 로 전달해야하기 때문에 최저가, 최고가를 미리 조회하여 해당 가격으로 데이터 조회 필요
        최저가, 최고가 조회에는 MIN(price), MAX(price) 를 활용하여 한 번의 쿼리로 보내는게 가장 효율적이기 때문에 해당 쿼리 적용
        */
        val (minPrice, maxPrice) = productRepository.findMinMaxPriceByCategory(category = category)

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
            .map(Brand::name)

        return LowestPrices(brandNames = brandNames, category = category, price = price)
    }
}
