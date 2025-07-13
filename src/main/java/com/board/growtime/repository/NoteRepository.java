package com.board.growtime.repository;

import com.board.growtime.entity.Note;
import com.board.growtime.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    // 사용자의 모든 회고 조회 (최신순)
    List<Note> findByUserOrderByCreatedAtDesc(User user);
    
    // 사용자의 회고 페이징 조회 (최신순)
    Page<Note> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 사용자와 ID로 회고 조회
    Optional<Note> findByIdAndUser(Long id, User user);
    
    // 사용자의 회고 개수 조회
    long countByUser(User user);
    
    // 제목으로 검색 (사용자별)
    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.title LIKE %:keyword% ORDER BY n.createdAt DESC")
    List<Note> findByUserAndTitleContaining(@Param("user") User user, @Param("keyword") String keyword);
    
    // 내용으로 검색 (사용자별)
    @Query("SELECT n FROM Note n WHERE n.user = :user AND n.content LIKE %:keyword% ORDER BY n.createdAt DESC")
    List<Note> findByUserAndContentContaining(@Param("user") User user, @Param("keyword") String keyword);
    
    // 제목 또는 내용으로 검색 (사용자별)
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%) ORDER BY n.createdAt DESC")
    List<Note> findByUserAndTitleOrContentContaining(@Param("user") User user, @Param("keyword") String keyword);
} 