package com.board.growtime.core

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
class BaseEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}