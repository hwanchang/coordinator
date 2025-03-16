package com.coordinator.product.domain

import com.coordinator.common.entity.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import java.math.BigDecimal
import org.hibernate.annotations.DynamicUpdate

@DynamicUpdate
@Entity
class Product(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long = 0,

    val brandId: Long,

    val name: String,

    @Enumerated(STRING)
    val category: Category,

    var price: BigDecimal,
) : BaseTimeEntity() {
    init {
        require(name.isNotBlank()) { "상품 이름은 공백일 수 없습니다." }
        require(price < BigDecimal.valueOf(1_000_000_000_000)) { "가격은 1,000,000,000,000원을 넘을 수 없습니다." }
    }

    fun update(price: BigDecimal) {
        this.price = price
    }
}
