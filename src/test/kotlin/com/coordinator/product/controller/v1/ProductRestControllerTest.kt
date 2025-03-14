package com.coordinator.product.controller.v1

import com.coordinator.common.BigDecimalPriceDeserializer
import com.coordinator.common.api.ApiResponse.Failure
import com.coordinator.common.api.ApiResponse.Success
import com.coordinator.fixtures.lowMaxPricesByBrand
import com.coordinator.fixtures.lowestPricesByBrand
import com.coordinator.fixtures.lowestPricesByCategory
import com.coordinator.product.controller.v1.data.ProductResponse
import com.coordinator.product.domain.Category.SOCKS
import com.coordinator.product.domain.Category.TOP
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
        val flyway = Flyway.configure()
            .dataSource("jdbc:h2:mem:coordinator;DB_CLOSE_DELAY=-1", "sa", "")
            .cleanDisabled(false)
            .load()
        flyway.clean()
        flyway.migrate()
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
            val response = mockMvc.post("/api/v1/products") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "brandId": 1,
                        "name": "Air Force 1",
                        "category": "스니커즈",
                        "price": 100000
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("상품이 정상적으로 생성되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result shouldBe "요청 성공"
            }
        }

        When("존재하지 않는 브랜드 id로 상품을 등록하면") {
            val response = mockMvc.post("/api/v1/products") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "brandId": 9999,
                        "name": "Air Force 1",
                        "category": "스니커즈",
                        "price": 100000
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Failure<String>>(response.contentAsString)

            Then("400 에러가 발생해야 한다.") {
                response.status shouldBe 400
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "9999: 해당 브랜드를 찾을 수 없습니다."
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
            val response = mockMvc.get("/api/v1/products/9999")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Failure<ProductResponse>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "productId - 9999: 해당 상품을 찾을 수 없습니다."
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
            val response = mockMvc.patch("/api/v1/products/9999") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "price": 150000
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Failure<String>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "productId - 9999: 해당 상품을 찾을 수 없습니다."
            }
        }

        When("유효한 상품의 가격을 변경하면") {
            val response = mockMvc.patch("/api/v1/products/4") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "price": 9000
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("카테고리 별 최저가(LowestPricesByCategory) 캐시가 업데이트 되고, 상품 가격이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result shouldBe "요청 성공"
                productRepository.findByIdOrNull(4)!!.price shouldBe BigDecimal(9000)
            }
        }

        When("유효한 상품의 가격을 변경하면") {
            val response = mockMvc.patch("/api/v1/products/17") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "price": 10
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("카테고리 별 최저가, 최고가(MinMaxPricesByCategory)의 최저가 변경으로 캐시가 업데이트 되고, 상품 가격이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result shouldBe "요청 성공"
                productRepository.findByIdOrNull(17)!!.price shouldBe BigDecimal(10)
            }
        }

        When("유효한 상품의 가격을 변경하면") {
            val response = mockMvc.patch("/api/v1/products/17") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "price": 20000
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("카테고리 별 최저가, 최고가(MinMaxPricesByCategory)의 최고가 변경으로 캐시가 업데이트 되고, 상품 가격이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result shouldBe "요청 성공"
                productRepository.findByIdOrNull(17)!!.price shouldBe BigDecimal(20000)
            }
        }

        When("유효한 상품의 가격을 변경하면") {
            val response = mockMvc.patch("/api/v1/products/4") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "price": 10
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("상품 가격이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result shouldBe "요청 성공"
                productRepository.findByIdOrNull(4)!!.price shouldBe BigDecimal(10)
            }
        }
    }

    Given("상품 삭제 요청이 들어올 때") {
        When("존재하지 않는 상품을 삭제하면") {
            val response = mockMvc.delete("/api/v1/products/9999")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Failure<String>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "productId - 9999: 해당 상품을 찾을 수 없습니다."
            }
        }

        When("유효한 상품을 삭제하면") {
            val response = mockMvc.delete("/api/v1/products/4")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("상품이 정상적으로 삭제되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result shouldBe "요청 성공"
                productRepository.findByIdOrNull(4) shouldBe null
            }
        }

        When("카테고리 별 최저가, 최고가(MinMaxPricesByCategory)의 최고가인 유효한 상품을 삭제하면") {
            mockMvc.get("/api/v1/products/min-max-prices") { param("category", SOCKS.name) }.andReturn()
            mockMvc.post("/api/v1/products") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "brandId": 9,
                        "name": "양말 아이템",
                        "category": "양말",
                        "price": 2000
                    }
                """.trimIndent()
            }.andReturn()
            val response = mockMvc.delete("/api/v1/products/71")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("캐시가 삭제되고, 상품이 정상적으로 삭제되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result shouldBe "요청 성공"
                productRepository.findByIdOrNull(71) shouldBe null
            }
        }
    }
})
