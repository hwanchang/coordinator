package com.coordinator.common.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
    @CreatedDate
    var createdAt: LocalDateTime = now()

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
}
