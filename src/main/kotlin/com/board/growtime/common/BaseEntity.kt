package com.board.growtime.common

import jakarta.persistence.*
import java.time.LocalDateTime

@MappedSuperclass
open class BaseEntity {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    
    @Column
    var updatedAt: LocalDateTime? = null
    
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
