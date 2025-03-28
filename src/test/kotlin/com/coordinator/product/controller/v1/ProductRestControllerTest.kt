package com.coordinator.product.controller.v1

import com.coordinator.common.BigDecimalPriceDeserializer
import com.coordinator.common.api.ApiResponse.Failure
import com.coordinator.common.api.ApiResponse.Success
import com.coordinator.fixtures.lowMaxPricesByBrand
import com.coordinator.fixtures.lowestPricesByBrand
import com.coordinator.fixtures.lowestPricesByCategory
import com.coordinator.fixtures.newLowMaxPricesByBrand
import com.coordinator.fixtures.newLowestPricesByBrand
import com.coordinator.fixtures.newLowestPricesByCategory
import com.coordinator.product.controller.v1.data.CreateProductRequest
import com.coordinator.product.controller.v1.data.ProductResponse
import com.coordinator.product.controller.v1.data.UpdateProductPriceRequest
import com.coordinator.product.domain.Category.ACCESSORY
import com.coordinator.product.domain.Category.HAT
import com.coordinator.product.domain.Category.OUTER
import com.coordinator.product.domain.Category.SNEAKERS
import com.coordinator.product.domain.Category.SOCKS
import com.coordinator.product.domain.Category.TOP
import com.coordinator.product.domain.Category.TROUSERS
import com.coordinator.product.repository.jpa.ProductRepository
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_CLASS)
@SpringBootTest
@TestInstance(PER_CLASS)
@Transactional
class ProductRestControllerTest(
    private val mockMvc: MockMvc,

    private val productRepository: ProductRepository,
) : BehaviorSpec({

    extension(SpringExtension)

    beforeSpec {
        Flyway.configure()
            .dataSource("jdbc:h2:mem:coordinator;DB_CLOSE_DELAY=-1", "sa", "")
            .cleanDisabled(false)
            .load()
            .apply {
                clean()
                migrate()
            }
    }

    val objectMapper = jacksonObjectMapper().apply {
        registerModule(SimpleModule().addDeserializer(BigDecimal::class.java, BigDecimalPriceDeserializer()))
        registerModule(JavaTimeModule())
    }

    Given("카테고리별 최저가 상품 조회 요청이 들어올 때") {
        When("카테고리별 최저가 상품을 조회하면") {
            val response = mockMvc.get("/api/v1/products/lowest-price-by-categories")
                .andReturn().response

            Then("정상적으로 최저가 상품 목록이 반환되어야 한다.") {
                response.status shouldBe 200
                response.contentAsString shouldBe lowestPricesByCategory
            }
        }
    }

    Given("브랜드별 최저가 상품 조회 요청이 들어올 때") {
        When("브랜드별 최저가 상품을 조회하면") {
            val response = mockMvc.get("/api/v1/products/lowest-price-by-brands")
                .andReturn().response

            Then("정상적으로 최저가 상품 목록이 반환되어야 한다.") {
                response.status shouldBe 200
                response.contentAsString shouldBe lowestPricesByBrand
            }
        }
    }

    Given("카테고리별 최저가, 최고가 상품 조회 요청이 들어올 때") {
        When("카테고리를 기준으로 최저가, 최고가 상품을 조회하면") {
            val response = mockMvc.get("/api/v1/products/min-max-prices") {
                param("category", TOP.name)
            }.andReturn().response

            Then("정상적으로 최저, 최고가 상품 정보가 반환되어야 한다.") {
                response.status shouldBe 200
                response.contentAsString shouldBe lowMaxPricesByBrand
            }
        }

        When("잘못된 카테고리를 요청하면") {
            val response = mockMvc.get("/api/v1/products/min-max-prices") {
                param("category", "장갑")
            }.andReturn().response

            Then("400 에러가 발생해야 한다.") {
                response.status shouldBe 400
            }
        }
    }

    Given("상품 생성 요청이 들어올 때") {
        When("유효한 상품 정보를 등록하면") {
            val request = CreateProductRequest(
                brandId = 1,
                name = "Air Force 1",
                category = SNEAKERS,
                price = BigDecimal(100000),
            )
            val response = mockMvc.post("/api/v1/products") {
                contentType = APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<ProductResponse>>(response.contentAsString)

            Then("상품이 정상적으로 생성되어야 한다.") {
                response.status shouldBe 201
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.brandId shouldBe 1
                apiResponse.result.name shouldBe "Air Force 1"
                apiResponse.result.category shouldBe SNEAKERS
                apiResponse.result.price shouldBe BigDecimal(100000)
            }
        }

        When("존재하지 않는 브랜드 id로 상품을 등록하면") {
            val request = CreateProductRequest(
                brandId = -1,
                name = "Air Force 1",
                category = SNEAKERS,
                price = BigDecimal(100000),
            )
            val response = mockMvc.post("/api/v1/products") {
                contentType = APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Failure<String>>(response.contentAsString)

            Then("400 에러가 발생해야 한다.") {
                response.status shouldBe 400
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "-1: 해당 브랜드를 찾을 수 없습니다."
            }
        }
    }

    Given("상품 조회 요청이 들어올 때") {
        When("등록된 모든 상품을 조회하면") {
            val response = mockMvc.get("/api/v1/products")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Success<List<ProductResponse>>>(response.contentAsString)

            Then("상품 리스트가 반환되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.size shouldBe 73
            }
        }

        When("존재하지 않는 상품 id를 조회하면") {
            val response = mockMvc.get("/api/v1/products/-1")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Failure<ProductResponse>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "productId - -1: 해당 상품을 찾을 수 없습니다."
            }
        }

        When("등록된 특정 상품을 조회하면") {
            val response = mockMvc.get("/api/v1/products/1")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Success<ProductResponse>>(response.contentAsString)

            Then("해당 브랜드 정보가 반환되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.id shouldBe 1
                apiResponse.result.category shouldBe TOP
                apiResponse.result.price shouldBe BigDecimal(11200)
            }
        }
    }

    Given("상품 수정 요청이 들어올 때") {
        When("존재하지 않는 상품의 가격을 변경하면") {
            val request = UpdateProductPriceRequest(
                price = BigDecimal(150000),
            )
            val response = mockMvc.patch("/api/v1/products/-1") {
                contentType = APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Failure<String>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "productId - -1: 해당 상품을 찾을 수 없습니다."
            }
        }

        When("카테고리의 최저가인 상품의 가격을 변경하면") {
            val request = UpdateProductPriceRequest(
                price = BigDecimal(3000),
            )
            val response = mockMvc.patch("/api/v1/products/30") {
                contentType = APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<ProductResponse>>(response.contentAsString)

            Then("카테고리별 최저가(LowestPricesByCategory) 캐시가 업데이트 되고, 상품 가격이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.brandId shouldBe 4
                apiResponse.result.name shouldBe "D-상품-6"
                apiResponse.result.category shouldBe HAT
                apiResponse.result.price shouldBe BigDecimal(3000)
                productRepository.findByIdOrNull(30)!!.price shouldBe BigDecimal(3000)
            }
        }

        When("카테고리별 최저가에 해당하는 상품의 가격을 변경하면") {
            val request = UpdateProductPriceRequest(
                price = BigDecimal(6000),
            )
            val response = mockMvc.patch("/api/v1/products/34") {
                contentType = APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<ProductResponse>>(response.contentAsString)

            Then("카테고리별 최저가, 최고가(MinMaxPricesByCategory)의 최저가 변경으로 캐시가 업데이트 되고, 상품 가격이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.brandId shouldBe 5
                apiResponse.result.name shouldBe "E-상품-2"
                apiResponse.result.category shouldBe OUTER
                apiResponse.result.price shouldBe BigDecimal(6000)
                productRepository.findByIdOrNull(34)!!.price shouldBe BigDecimal(6000)
            }
        }

        When("카테고리별 최고가에 해당하는 상품의 가격을 변경하면") {
            val request = UpdateProductPriceRequest(
                price = BigDecimal(2100),
            )
            val response = mockMvc.patch("/api/v1/products/3") {
                contentType = APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<ProductResponse>>(response.contentAsString)

            Then("카테고리별 최저가, 최고가(MinMaxPricesByCategory)의 최고가 변경으로 캐시가 업데이트 되고, 상품 가격이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.brandId shouldBe 1
                apiResponse.result.name shouldBe "A-상품-3"
                apiResponse.result.category shouldBe TROUSERS
                apiResponse.result.price shouldBe BigDecimal(2100)
                productRepository.findByIdOrNull(3)!!.price shouldBe BigDecimal(2100)
            }
        }

        When("유효한 상품의 가격을 변경하면") {
            val request = UpdateProductPriceRequest(
                price = BigDecimal(2300),
            )
            val response = mockMvc.patch("/api/v1/products/72") {
                contentType = APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<ProductResponse>>(response.contentAsString)

            Then("상품 가격이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.brandId shouldBe 9
                apiResponse.result.name shouldBe "I-상품-8"
                apiResponse.result.category shouldBe ACCESSORY
                apiResponse.result.price shouldBe BigDecimal(2300)
                productRepository.findByIdOrNull(72)!!.price shouldBe BigDecimal(2300)
            }
        }
    }

    Given("상품 삭제 요청이 들어올 때") {
        When("존재하지 않는 상품을 삭제하면") {
            val response = mockMvc.delete("/api/v1/products/-1")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Failure<String>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "productId - -1: 해당 상품을 찾을 수 없습니다."
            }
        }

        When("유효한 상품을 삭제하면") {
            val response = mockMvc.delete("/api/v1/products/4")
                .andReturn().response

            Then("상품이 정상적으로 삭제되어야 한다.") {
                response.status shouldBe 204
                productRepository.findByIdOrNull(4) shouldBe null
            }
        }

        When("카테고리별 최저가, 최고가(MinMaxPricesByCategory)의 최고가인 유효한 상품을 삭제하면") {
            mockMvc.get("/api/v1/products/min-max-prices") { param("category", SOCKS.name) }.andReturn()
            val request = CreateProductRequest(
                brandId = 9,
                name = "양말 아이템",
                category = SOCKS,
                price = BigDecimal(2000),
            )
            mockMvc.post("/api/v1/products") {
                contentType = APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andReturn()
            val response = mockMvc.delete("/api/v1/products/71")
                .andReturn().response

            Then("캐시가 삭제되고, 상품이 정상적으로 삭제되어야 한다.") {
                response.status shouldBe 204
                productRepository.findByIdOrNull(71) shouldBe null
            }
        }
    }

    Given("다시 카테고리별 최저가 상품 조회 요청이 들어올 때") {
        When("카테고리별 최저가 상품을 조회하면") {
            val response = mockMvc.get("/api/v1/products/lowest-price-by-categories")
                .andReturn().response

            Then("새로운 최저가 상품 목록이 반환되어야 한다.") {
                response.status shouldBe 200
                response.contentAsString shouldBe newLowestPricesByCategory
            }
        }
    }

    Given("다시 브랜드별 최저가 상품 조회 요청이 들어올 때") {
        When("브랜드별 최저가 상품을 조회하면") {
            val response = mockMvc.get("/api/v1/products/lowest-price-by-brands")
                .andReturn().response

            Then("새로운 최저가 상품 목록이 반환되어야 한다.") {
                response.status shouldBe 200
                response.contentAsString shouldBe newLowestPricesByBrand
            }
        }
    }

    Given("다시 카테고리별 최저가, 최고가 상품 조회 요청이 들어올 때") {
        When("카테고리를 기준으로 최저가, 최고가 상품을 조회하면") {
            val response = mockMvc.get("/api/v1/products/min-max-prices") {
                param("category", SNEAKERS.name)
            }.andReturn().response

            Then("새로운 최저, 최고가 상품 정보가 반환되어야 한다.") {
                response.status shouldBe 200
                response.contentAsString shouldBe newLowMaxPricesByBrand
            }
        }
    }
})
