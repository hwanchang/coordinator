package com.coordinator.brand.service

import com.coordinator.brand.domain.Brand
import com.coordinator.brand.repository.BrandRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.data.repository.findByIdOrNull

@TestInstance(PER_CLASS)
class BrandServiceTest : BehaviorSpec({
    val brandRepository: BrandRepository = mockk()
    val brandService = BrandService(brandRepository)

    Given("브랜드를 생성할 때") {
        val brand = Brand(name = "Nike")

        When("새로운 브랜드를 생성하면") {
            every { brandRepository.existsByName(brand.name) } returns false
            every { brandRepository.save(brand) } returns brand

            brandService.createBrand(brand)

            Then("브랜드가 정상적으로 저장되어야 한다.") {
                verify { brandRepository.existsByName(brand.name) }
                verify { brandRepository.save(brand) }
            }
        }

        When("이미 존재하는 브랜드 이름으로 생성하면") {
            every { brandRepository.existsByName(brand.name) } returns true

            Then("예외가 발생해야 한다.") {
                shouldThrow<IllegalStateException> {
                    brandService.createBrand(brand)
                }.message shouldBe "Nike: 동일한 브랜드 명이 존재합니다."
            }
        }
    }

    Given("브랜드를 조회할 때") {
        val brand = Brand(id = 1L, name = "Adidas")

        When("모든 상품을 조회하면") {
            every { brandRepository.findAll() } returns listOf(brand)

            val fountBrands = brandService.getBrands()

            Then("상품 리스트가 반환되어야 한다.") {
                fountBrands shouldBe listOf(brand)
            }
        }

        When("id로 조회를 하면") {
            every { brandRepository.findByIdOrNull(1L) } returns brand

            val foundBrand = brandService.getBrand(1L)

            Then("해당하는 id의 브랜드를 반환해야 한다.") {
                foundBrand.shouldNotBeNull()
                foundBrand.id shouldBe 1L
                foundBrand.name shouldBe "Adidas"
            }
        }

        When("존재하지 않는 id로 조회하면") {
            every { brandRepository.findByIdOrNull(2L) } returns null

            Then("예외가 발생해야 한다.") {
                shouldThrow<EntityNotFoundException> {
                    brandService.getBrand(2L)
                }.message shouldBe "2: 해당 브랜드를 찾을 수 없습니다."
            }
        }
    }

    Given("브랜드를 업데이트할 때") {
        val brand = spyk(Brand(id = 1L, name = "NewBalance"))

        When("브랜드 명을 변경하면") {
            every { brandRepository.existsByName(any<String>()) } returns false
            every { brandRepository.findByIdOrNull(1L) } returns brand
            every { brandRepository.save(any<Brand>()) } returns brand

            brandService.updateBrand(1L, "Vans")

            Then("브랜드 이름이 변경되어야 한다.") {
                verify { brandRepository.findByIdOrNull(1L) }
                verify { brand.update("Vans") }
                verify { brandRepository.save(brand) }
            }
        }

        When("이미 존재하는 브랜드 명으로 변경하면") {
            every { brandRepository.existsByName(any<String>()) } returns true

            Then("예외가 발생해야 한다.") {
                shouldThrow<IllegalStateException> {
                    brandService.updateBrand(1L, "Vans")
                }.message shouldBe "Vans: 이미 존재하는 브랜드 명으로 수정할 수 없습니다."
            }
        }
    }

    Given("브랜드를 삭제할 때") {
        val brand = Brand(id = 1L, name = "Stussy")

        When("존재하는 브랜드를 삭제하면") {
            every { brandRepository.findByIdOrNull(1L) } returns brand
            every { brandRepository.delete(brand) } just Runs

            brandService.deleteBrand(1L)

            Then("브랜드가 삭제되어야 한다.") {
                verify { brandRepository.delete(brand) }
            }
        }

        When("존재하지 않는 브랜드를 삭제하면") {
            every { brandRepository.findByIdOrNull(2L) } returns null

            Then("예외가 발생해야 한다.") {
                shouldThrow<EntityNotFoundException> {
                    brandService.deleteBrand(2L)
                }.message shouldBe "2: 해당 브랜드를 찾을 수 없습니다."
            }
        }
    }

    Given("id를 활용하여 브랜드의 존재 여부를 확인할 때") {
        When("해당 id의 브랜드가 존재하면") {
            every { brandRepository.existsById(1L) } returns true

            Then("true를 반환해야 한다.") {
                brandService.existsById(1L) shouldBe true
            }
        }

        When("해당 id의 브랜드가 존재하지 않으면") {
            every { brandRepository.existsById(2L) } returns false

            Then("false를 반환해야 한다.") {
                brandService.existsById(2L) shouldBe false
            }
        }
    }

    Given("여러 브랜드들의 id가 주어질 때") {
        val brands = listOf(
            Brand(id = 1L, name = "Nike"),
            Brand(id = 2L, name = "Adidas")
        )

        When("브랜드 id들로 조회하면") {
            every { brandRepository.findAllByIdIn(listOf(1L, 2L)) } returns brands

            val foundBrands = brandService.getAllBrandsByIds(listOf(1L, 2L))

            Then("해당하는 브랜드들의 리스트를 반환해야 한다.") {
                foundBrands.shouldContainExactly(brands)
            }
        }
    }
})
