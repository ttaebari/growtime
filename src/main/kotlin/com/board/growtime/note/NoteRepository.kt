package com.board.growtime.note

import com.board.growtime.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    
    // 사용자의 모든 회고 조회 (최신순)
    fun findByUserOrderByCreatedAtDesc(user: User): List<Note>
    
    // 사용자의 회고 페이징 조회 (최신순)
    fun findByUserOrderByCreatedAtDesc(user: User, pageable: Pageable): Page<Note>
    
    // 사용자와 ID로 회고 조회
    fun findByIdAndUser(id: Long, user: User): Optional<Note>
    
    // 사용자의 회고 개수 조회
    fun countByUser(user: User): Long
    
    // 제목 또는 내용으로 검색 (사용자별)
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%) ORDER BY n.createdAt DESC")
    fun findByUserAndTitleOrContentContaining(@Param("user") user: User, @Param("keyword") keyword: String): List<Note>
} 