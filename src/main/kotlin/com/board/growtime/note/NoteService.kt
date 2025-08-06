package com.board.growtime.note

import com.board.growtime.user.User
import com.board.growtime.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NoteService(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(NoteService::class.java)

    /**
     * 회고 작성
     */
    fun createNote(githubId: String, request: Map<String, String>): Map<String, Any> {
        val title = request["title"]?.trim()
        val content = request["content"]?.trim()
        
        // 최소한의 서버 검증 (프론트엔드 검증의 백업)
        if (title.isNullOrBlank()) {
            throw IllegalArgumentException("제목은 필수입니다.")
        }
        if (content.isNullOrBlank()) {
            throw IllegalArgumentException("내용은 필수입니다.")
        }
        
        val user = findUserByGithubId(githubId)
        val note = Note(title, content, user)
        val savedNote = noteRepository.save(note)
        
        log.info("회고 작성 완료: 사용자={}, 제목={}, ID={}", githubId, title, savedNote.id)
        
        return mapOf(
            "message" to "회고가 성공적으로 작성되었습니다.",
            "note" to createNoteResponse(savedNote)
        )
    }

    /**
     * 회고 목록 조회 (최신순)
     */
    @Transactional(readOnly = true)
    fun getNotes(githubId: String): List<Note> {
        val user = userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
        
        return noteRepository.findByUserOrderByCreatedAtDesc(user)
    }

    /**
     * 회고 페이징 조회
     */
    @Transactional(readOnly = true)
    fun getNotesWithPaging(githubId: String, pageable: Pageable): Map<String, Any> {
        val user = findUserByGithubId(githubId)
        val notesPage = noteRepository.findByUserOrderByCreatedAtDesc(user, pageable)
        
        return mapOf(
            "notes" to notesPage.content.map { createNoteResponse(it) },
            "totalElements" to notesPage.totalElements,
            "totalPages" to notesPage.totalPages,
            "currentPage" to notesPage.number,
            "size" to notesPage.size
        )
    }

    /**
     * 회고 상세 조회
     */
    @Transactional(readOnly = true)
    fun getNote(githubId: String, noteId: Long): Map<String, Any> {
        val user = findUserByGithubId(githubId)
        val note = noteRepository.findByIdAndUser(noteId, user)
            .orElseThrow { IllegalArgumentException("회고를 찾을 수 없습니다: $noteId") }
        
        return mapOf("note" to createNoteResponse(note))
    }

    /**
     * 회고 수정
     */
    fun updateNote(githubId: String, noteId: Long, request: Map<String, String>): Map<String, Any> {
        val title = request["title"]?.trim()
        val content = request["content"]?.trim()
        
        // 최소한의 서버 검증
        if (title.isNullOrBlank()) {
            throw IllegalArgumentException("제목은 필수입니다.")
        }
        if (content.isNullOrBlank()) {
            throw IllegalArgumentException("내용은 필수입니다.")
        }
        
        val user = findUserByGithubId(githubId)
        val note = noteRepository.findByIdAndUser(noteId, user)
            .orElseThrow { IllegalArgumentException("회고를 찾을 수 없습니다: $noteId") }
        
        note.updateNote(title, content)
        val updatedNote = noteRepository.save(note)
        
        log.info("회고 수정 완료: 사용자={}, 제목={}, ID={}", githubId, title, noteId)
        
        return mapOf(
            "message" to "회고가 성공적으로 수정되었습니다.",
            "note" to createNoteResponse(updatedNote)
        )
    }

    /**
     * 회고 삭제
     */
    fun deleteNote(githubId: String, noteId: Long): Map<String, Any> {
        val user = findUserByGithubId(githubId)
        val note = noteRepository.findByIdAndUser(noteId, user)
            .orElseThrow { IllegalArgumentException("회고를 찾을 수 없습니다: $noteId") }
        
        noteRepository.delete(note)
        log.info("회고 삭제 완료: 사용자={}, ID={}", githubId, noteId)
        
        return mapOf("message" to "회고가 성공적으로 삭제되었습니다.")
    }

    /**
     * 회고 검색 (제목 또는 내용)
     */
    @Transactional(readOnly = true)
    fun searchNotes(githubId: String, keyword: String): Map<String, Any> {
        val user = findUserByGithubId(githubId)
        val notes = noteRepository.findByUserAndTitleOrContentContaining(user, keyword.trim())
        
        return mapOf(
            "notes" to notes.map { createNoteResponse(it, includeUpdatedAt = false) },
            "totalCount" to notes.size
        )
    }

    /**
     * 사용자의 회고 개수 조회
     */
    @Transactional(readOnly = true)
    fun getNoteCount(githubId: String): Map<String, Any> {
        val user = findUserByGithubId(githubId)
        val count = noteRepository.countByUser(user)
        
        return mapOf("count" to count)
    }

    // === Private Helper Methods ===

    /**
     * GitHub ID로 사용자 찾기 (공통 로직)
     */
    private fun findUserByGithubId(githubId: String): User {
        return userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
    }

    /**
     * Note 응답 객체 생성 (공통 로직)
     */
    private fun createNoteResponse(note: Note, includeUpdatedAt: Boolean = true): Map<String, Any?> {
        val response = mutableMapOf<String, Any?>(
            "id" to note.id,
            "title" to note.title,
            "content" to note.content,
            "createdAt" to note.createdAt
        )
        
        if (includeUpdatedAt) {
            response["updatedAt"] = note.updatedAt
        }
        
        return response
    }
} 