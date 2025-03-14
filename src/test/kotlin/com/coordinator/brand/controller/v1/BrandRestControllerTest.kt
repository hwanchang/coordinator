package com.coordinator.brand.controller.v1

import com.coordinator.brand.controller.v1.data.BrandResponse
import com.coordinator.brand.repository.BrandRepository
import com.coordinator.common.api.ApiResponse.Failure
import com.coordinator.common.api.ApiResponse.Success
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
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
class BrandRestControllerTest(
    private val mockMvc: MockMvc,

    private val brandRepository: BrandRepository,
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

    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    Given("브랜드 생성 요청이 들어올 때") {
        When("기존과 동일한 이름의 브랜드를 등록하면") {
            val response = mockMvc.post("/api/v1/brands") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "name": "A"
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Failure<String>>(response.contentAsString)

            Then("400 에러가 발생해야 한다.") {
                response.status shouldBe 400
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "A: 동일한 브랜드 명이 존재합니다."
                brandRepository.count() shouldBe 9
            }
        }

        When("새로운 브랜드를 등록하면") {
            val response = mockMvc.post("/api/v1/brands") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "name": "Nike"
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("브랜드가 정상적으로 생성되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result shouldBe "요청 성공"
                brandRepository.count() shouldBe 10
            }
        }
    }

    Given("브랜드 조회 요청이 들어올 때") {
        When("등록된 모든 브랜드를 조회하면") {
            val response = mockMvc.get("/api/v1/brands")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Success<List<BrandResponse>>>(response.contentAsString)

            Then("등록된 브랜드 목록이 반환되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.size shouldBe 10
            }
        }

        When("존재하지 않는 id로 브랜드를 조회하면") {
            val response = mockMvc.get("/api/v1/brands/9999")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Failure<BrandResponse>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "9999: 해당 브랜드를 찾을 수 없습니다."
            }
        }

        When("등록된 특정 브랜드를 조회하면") {
            val response = mockMvc.get("/api/v1/brands/1")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Success<BrandResponse>>(response.contentAsString)

            Then("해당 브랜드 정보가 반환되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                apiResponse.result.id shouldBe 1
                apiResponse.result.name shouldBe "A"
            }
        }
    }

    Given("브랜드 수정 요청이 들어올 때") {
        When("잘못된 id로 브랜드를 수정하면") {
            val response = mockMvc.patch("/api/v1/brands/9999") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "name": "Adidas"
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Failure<BrandResponse>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "9999: 해당 브랜드를 찾을 수 없습니다."
            }
        }

        When("이미 존재하는 브랜드 명으로 수정하면") {
            val response = mockMvc.patch("/api/v1/brands/10") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "name": "A"
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Failure<String>>(response.contentAsString)

            Then("400 에러가 발생해야 한다.") {
                response.status shouldBe 400
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "A: 이미 존재하는 브랜드 명으로 수정할 수 없습니다."
            }
        }

        When("가능한 브랜드 명을 수정하면") {
            val response = mockMvc.patch("/api/v1/brands/10") {
                contentType = APPLICATION_JSON
                content = """
                    {
                        "name": "Adidas"
                    }
                """.trimIndent()
            }.andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("브랜드 이름이 정상적으로 변경되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                brandRepository.findByIdOrNull(10)!!.name shouldBe "Adidas"
            }
        }
    }

    Given("브랜드 삭제 요청이 들어올 때") {
        When("잘못된 id로 브랜드를 삭제하면") {
            val response = mockMvc.delete("/api/v1/brands/9999")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Failure<BrandResponse>>(response.contentAsString)

            Then("404 에러가 발생해야 한다.") {
                response.status shouldBe 404
                apiResponse.isSucceeded shouldBe false
                apiResponse.errorMessage shouldBe "9999: 해당 브랜드를 찾을 수 없습니다."
            }
        }

        When("브랜드를 삭제하면") {
            val response = mockMvc.delete("/api/v1/brands/10")
                .andReturn().response
            val apiResponse = objectMapper.readValue<Success<String>>(response.contentAsString)

            Then("브랜드가 정상적으로 삭제되어야 한다.") {
                response.status shouldBe 200
                apiResponse.isSucceeded shouldBe true
                brandRepository.findByIdOrNull(10) shouldBe null
            }
        }
    }
})
