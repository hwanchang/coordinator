package com.coordinator.product.service

import com.coordinator.brand.domain.Brand
import com.coordinator.brand.service.BrandService
import com.coordinator.product.domain.Category
import com.coordinator.product.domain.Category.SNEAKERS
import com.coordinator.product.domain.Product
import com.coordinator.product.domain.data.lowestpricesbybrand.LowestPricesByBrand
import com.coordinator.product.domain.data.lowestpricesbycategory.LowestPrices
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrice
import com.coordinator.product.domain.data.minmaxprices.MinMaxPrices
import com.coordinator.product.repository.jpa.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import java.math.BigDecimal
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.data.repository.findByIdOrNull

@TestInstance(PER_CLASS)
class ProductServiceTest : BehaviorSpec({
    val brandService: BrandService = mockk()
    val productRepository: ProductRepository = mockk()
    val productCache: ProductCache = mockk(relaxed = true)
    val productService = ProductService(brandService, productRepository, productCache)

    Given("상품을 생성할 때") {
        val product = Product(
            id = 1L,
            brandId = 1L,
            name = "Air Force 1",
            category = SNEAKERS,
            price = BigDecimal(100),
        )

        When("유효한 브랜드 id로 생성하면") {
            every { brandService.existsById(1L) } returns true
            every { productRepository.save(product) } returns product
            justRun { productCache.updateCache(product) }

            productService.createProduct(product)

            Then("상품이 저장되고 캐시에 반영되어야 한다.") {
                verify { brandService.existsById(1L) }
                verify { productRepository.save(product) }
                verify { productCache.updateCache(product) }
            }
        }

        When("존재하지 않는 브랜드 id로 생성하면") {
            every { brandService.existsById(1L) } returns false

            Then("예외가 발생해야 한다.") {
                shouldThrow<IllegalArgumentException> {
                    productService.createProduct(product)
                }.message shouldBe "1: 해당 브랜드를 찾을 수 없습니다."
            }
        }
    }

    Given("상품을 조회할 때") {
        val product = Product(
            id = 1L,
            brandId = 1L,
            name = "Air Force 1",
            category = SNEAKERS,
            price = BigDecimal(100),
        )

        When("모든 상품을 조회하면") {
            every { productRepository.findAll() } returns listOf(product)

            val foundProducts = productService.getProducts()

            Then("상품 리스트가 반환되어야 한다.") {
                foundProducts shouldBe listOf(product)
            }
        }

        When("존재하는 상품을 id로 조회하면") {
            every { productRepository.findByIdOrNull(1L) } returns product

            val foundProduct = productService.getProduct(1L)

            Then("상품이 정상적으로 반환되어야 한다.") {
                foundProduct shouldBe product
            }
        }

        When("존재하지 않는 상품을 조회하면") {
            every { productRepository.findByIdOrNull(2L) } returns null

            Then("예외가 발생해야 한다.") {
                shouldThrow<EntityNotFoundException> {
                    productService.getProduct(2L)
                }.message shouldBe "productId - 2: 해당 상품을 찾을 수 없습니다."
            }
        }
    }

    Given("상품을 업데이트할 때") {
        val product = spyk(
            Product(
                id = 1L,
                brandId = 1L,
                name = "Air Force 1",
                category = SNEAKERS,
                price = BigDecimal(100),
            )
        )

        When("상품 가격을 변경하면") {
            every { productRepository.findByIdOrNull(1L) } returns product
            every { productRepository.save(product) } returns product
            justRun { productCache.updateCache(product) }

            productService.updateProduct(1L, BigDecimal(150))

            Then("상품 가격이 변경되고 저장되어야 한다.") {
                verify { productRepository.findByIdOrNull(1L) }
                verify { product.update(BigDecimal(150)) }
                verify { productRepository.save(product) }
                verify { productCache.updateCache(product) }
            }
        }
    }

    Given("상품을 삭제할 때") {
        val product = Product(
            id = 1L,
            brandId = 1L,
            name = "Air Force 1",
            category = SNEAKERS,
            price = BigDecimal(100),
        )

        When("동일 브랜드의 동일 카테고리에 상품이 두 개 이상이 있는 경우 한 개를 삭제하면") {
            every { productRepository.findByIdOrNull(1L) } returns product
            every { productRepository.countByBrandIdAndCategory(1L, SNEAKERS) } returns 2
            justRun { productRepository.delete(product) }
            justRun { productCache.deleteCache(product) }

            productService.deleteProduct(1L)

            Then("상품이 정상적으로 삭제되어야 한다.") {
                verify { productRepository.delete(product) }
                verify { productCache.deleteCache(product) }
            }
        }

        When("해당 브랜드의 마지막 상품을 삭제하려고 하면") {
            every { productRepository.findByIdOrNull(1L) } returns product
            every { productRepository.countByBrandIdAndCategory(1L, SNEAKERS) } returns 1

            Then("예외가 발생해야 한다.") {
                shouldThrow<IllegalStateException> {
                    productService.deleteProduct(1L)
                }.message shouldBe "상품이 한 개인 경우는 삭제할 수 없습니다."
            }
        }
    }

    Given("카테고리별 최저가를 조회할 때") {
        val lowestPrices = Category.entries.map { category ->
            LowestPrices(brandNames = setOf("Nike"), category = category, price = BigDecimal(100))
        }

        When("캐시에 데이터가 있으면") {
            lowestPrices.forEach {
                every { productCache.getLowestPriceByCategoryCache(it.category) } returns it
            }

            val lowestPricesByCategory = productService.getLowestPricesByCategory()

            Then("캐시 데이터를 반환해야 한다.") {
                Category.entries.forEach { category ->
                    verify { productCache.getLowestPriceByCategoryCache(category) }
                    verify(exactly = 0) { productRepository.findMinPriceByCategory(category) }
                }
                lowestPricesByCategory.lowestPriceProducts shouldContainExactly lowestPrices
                lowestPricesByCategory.totalPrice shouldBe lowestPrices.sumOf(LowestPrices::price)
            }
        }

        When("캐시에 데이터가 없으면") {
            val products = Category.entries.mapIndexed { index, category ->
                Product(
                    brandId = index + 1L,
                    name = "product: $category",
                    category = category,
                    price = BigDecimal(100),
                )
            }
            every { productCache.getLowestPriceByCategoryCache(any<Category>()) } returns null
            every { productRepository.findMinPriceByCategory(any<Category>()) } returns BigDecimal(100)
            Category.entries.map { category ->
                every {
                    productRepository.findAllByCategoryAndPrice(category, BigDecimal(100))
                } returns products.filter { it.category == category }
            }

            every { brandService.getAllBrandsByIds(any<List<Long>>()) } returns Category.entries.mapIndexed { index, category ->
                Brand(id = index + 1L, name = "brand: $category")
            }
            justRun { productCache.saveLowestPriceByCategoryCache(any<LowestPrices>()) }

            val lowestPricesByCategory = productService.getLowestPricesByCategory()

            Then("DB 조회 후 캐시에 저장해야 한다.") {
                lowestPricesByCategory.totalPrice shouldBe products.sumOf(Product::price)
                verify { productCache.saveLowestPriceByCategoryCache(any<LowestPrices>()) }
            }
        }

        When("캐시에 데이터가 없고, 카테고리에 상품이 없으면") {
            every { productCache.getLowestPriceByCategoryCache(any<Category>()) } returns null
            every { productRepository.findMinPriceByCategory(any<Category>()) } returns null

            Then("예외가 발생해야 한다.") {
                shouldThrow<EntityNotFoundException> {
                    productService.getLowestPricesByCategory()
                }.message shouldBe "category - ${Category.entries.first()}: 해당 상품을 찾을 수 없습니다."
            }
        }
    }

    Given("브랜드별 최저가 상품을 조회할 때") {
        val brands = listOf(
            Brand(id = 1L, name = "Nike"),
            Brand(id = 2L, name = "Adidas")
        )

        When("캐시에 데이터가 있으면") {
            every { brandService.getBrands() } returns brands
            every { productCache.getLowestPriceByBrandCache("Nike") } returns LowestPricesByBrand(
                brandName = "Nike",
                products = Category.entries.map { category ->
                    Product(
                        brandId = 1L,
                        name = "Nike: product: $category",
                        category = category,
                        price = BigDecimal(100),
                    )
                }
            )
            every { productCache.getLowestPriceByBrandCache("Adidas") } returns LowestPricesByBrand(
                brandName = "Adidas",
                products = Category.entries.map { category ->
                    Product(
                        brandId = 2L,
                        name = "Adidas: product: $category",
                        category = category,
                        price = BigDecimal(110),
                    )
                }
            )

            val lowestPricesByBrand = productService.getLowestPricesByBrand()

            Then("캐시 데이터를 사용하여 최저가 브랜드를 반환해야 한다.") {
                lowestPricesByBrand.brandName shouldBe "Nike"
                lowestPricesByBrand.totalPrice shouldBe BigDecimal(100).multiply(Category.entries.size.toBigDecimal())

                verify { productCache.getLowestPriceByBrandCache("Nike") }
                verify { productCache.getLowestPriceByBrandCache("Adidas") }
                verify(exactly = 0) {
                    productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(any<Long>(), any<Category>())
                }
            }
        }

        When("캐시에 데이터가 없으면") {
            every { brandService.getBrands() } returns brands
            every { productCache.getLowestPriceByBrandCache(any<String>()) } returns null
            Category.entries.forEach { category ->
                every { productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(1L, category) } returns Product(
                    brandId = 1L, name = "Nike: product: $category", category = category, price = BigDecimal(100)
                )
                every { productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(2L, category) } returns Product(
                    brandId = 2L, name = "Adidas: product: $category", category = category, price = BigDecimal(110)
                )
            }
            justRun { productCache.saveLowestPriceByBrandCache(any<LowestPricesByBrand>()) }

            val lowestPricesByBrand = productService.getLowestPricesByBrand()

            Then("DB 조회 후 캐시에 저장하고 최저가 브랜드를 반환해야 한다.") {
                lowestPricesByBrand.brandName shouldBe "Nike"
                lowestPricesByBrand.totalPrice shouldBe BigDecimal(100).multiply(Category.entries.size.toBigDecimal())


                Category.entries.forEach { category ->
                    verify { productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(1L, category) }
                    verify { productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(2L, category) }
                }
                verify { productCache.saveLowestPriceByBrandCache(any<LowestPricesByBrand>()) }
            }
        }

        When("캐시에 데이터가 없고, 브랜드에 해당 카테고리의 상품이 없으면") {
            every { brandService.getBrands() } returns brands
            every { productCache.getLowestPriceByBrandCache(any<String>()) } returns null
            Category.entries.forEach { category ->
                every { productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(1L, category) } returns null
                every { productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(2L, category) } returns Product(
                    brandId = 2L, name = "Adidas: product: $category", category = category, price = BigDecimal(110)
                )
            }
            val lowestPricesByBrand = productService.getLowestPricesByBrand()

            Then("해당 브랜드를 제외한 브랜드 중 최저가 브랜드를 반환해야 한다.") {
                lowestPricesByBrand.brandName shouldBe "Adidas"
                lowestPricesByBrand.totalPrice shouldBe BigDecimal(110).multiply(Category.entries.size.toBigDecimal())

                Category.entries.forEach { category ->
                    verify { productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(1L, category) }
                    verify { productRepository.findFirstByBrandIdAndCategoryOrderByPriceAsc(2L, category) }
                }
                verify { productCache.saveLowestPriceByBrandCache(any<LowestPricesByBrand>()) }
            }
        }
    }

    Given("카테고리별 최저가, 최고가를 조회할 때") {
        val category = SNEAKERS
        val minMaxPrices = MinMaxPrices(
            category = category,
            minPrice = LowestPrices(brandNames = setOf("Nike"), category = category, price = BigDecimal(100)),
            maxPrice = LowestPrices(brandNames = setOf("Adidas"), category = category, price = BigDecimal(200)),
        )

        When("캐시에 데이터가 있으면") {
            every {
                productCache.getMinMaxPriceByCategoryCache(category)
            } returns MinMaxPrice(BigDecimal(100), BigDecimal(200))
            every {
                productRepository.findAllByCategoryAndPrice(category, BigDecimal(100))
            } returns listOf(Product(brandId = 1L, name = "Nike Shoe", category = category, price = BigDecimal(100)))
            every {
                productRepository.findAllByCategoryAndPrice(category, BigDecimal(200))
            } returns listOf(Product(brandId = 2L, name = "Adidas Shoe", category = category, price = BigDecimal(200)))
            every { brandService.getAllBrandsByIds(listOf(1L)) } returns listOf(Brand(id = 1L, name = "Nike"))
            every { brandService.getAllBrandsByIds(listOf(2L)) } returns listOf(Brand(id = 2L, name = "Adidas"))

            val foundMinMaxPrices = productService.getMinPriceAndMaxPriceByCategory(category)

            Then("캐시 데이터를 반환해야 한다.") {
                foundMinMaxPrices shouldBe minMaxPrices
            }
        }

        When("캐시에 데이터가 없으면") {
            every { productCache.getMinMaxPriceByCategoryCache(category) } returns null
            every {
                productRepository.findMinMaxPriceByCategory(category)
            } returns MinMaxPrice(BigDecimal(100), BigDecimal(200))
            every {
                productRepository.findAllByCategoryAndPrice(category, BigDecimal(100))
            } returns listOf(Product(brandId = 1L, name = "Nike Shoe", category = category, price = BigDecimal(100)))
            every {
                productRepository.findAllByCategoryAndPrice(category, BigDecimal(200))
            } returns listOf(Product(brandId = 2L, name = "Adidas Shoe", category = category, price = BigDecimal(200)))
            every { brandService.getAllBrandsByIds(listOf(1L)) } returns listOf(Brand(id = 1L, name = "Nike"))
            every { brandService.getAllBrandsByIds(listOf(2L)) } returns listOf(Brand(id = 2L, name = "Adidas"))
            justRun { productCache.saveMinMaxPriceByCategoryCache(any<Category>(), any<MinMaxPrice>()) }

            val foundMinMaxPrices = productService.getMinPriceAndMaxPriceByCategory(category)

            Then("DB 조회 후 캐시에 저장하고 최저가, 최고가 정보를 반환해야 한다.") {
                foundMinMaxPrices shouldBe minMaxPrices
                verify { productCache.saveMinMaxPriceByCategoryCache(any<Category>(), any<MinMaxPrice>()) }
            }
        }
    }
})
