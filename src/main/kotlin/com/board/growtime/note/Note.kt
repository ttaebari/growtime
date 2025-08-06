package com.board.growtime.note

import com.board.growtime.user.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "notes")
@EntityListeners(AuditingEntityListener::class)
class Note(
    @Column(nullable = false)
    var title: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
        protected set

    // 기본 생성자 (JPA용)
    protected constructor() : this("", "", User("", ""))

    // 회고 내용 업데이트
    fun updateNote(title: String, content: String) {
        this.title = title
        this.content = content
    }
} 