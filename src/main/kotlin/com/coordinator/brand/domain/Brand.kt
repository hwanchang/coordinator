package com.coordinator.brand.domain

import com.coordinator.common.entity.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import org.hibernate.annotations.DynamicUpdate

@DynamicUpdate
@Entity
class Brand(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long = 0,

    var name: String,
) : BaseTimeEntity() {
    fun update(name: String) {
        this.name = name
    }
}
