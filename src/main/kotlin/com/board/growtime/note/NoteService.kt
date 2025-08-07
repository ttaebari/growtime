package com.board.growtime.note

import com.board.growtime.note.dto.*
import com.board.growtime.note.result.*
import com.board.growtime.user.User
import com.board.growtime.user.UserRepository
import org.slf4j.LoggerFactory
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
    fun createNote(githubId: String, request: CreateNoteRequest): CreateNoteResult {
        // 입력 값 검증
        val title = request.title.trim()
        val content = request.content.trim()
        
        if (title.isBlank()) {
            return CreateNoteResult.ValidationError("제목은 필수입니다.")
        }
        if (content.isBlank()) {
            return CreateNoteResult.ValidationError("내용은 필수입니다.")
        }
        
        // 사용자 조회
        val userOpt = userRepository.findByGithubId(githubId)
        if (userOpt.isEmpty) {
            return CreateNoteResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val note = Note(title, content, user)
        val savedNote = noteRepository.save(note)
        
        log.info("회고 작성 완료: 사용자={}, 제목={}, ID={}", githubId, title, savedNote.id)
        
        return CreateNoteResult.Success(
            note = savedNote.toNoteInfo(),
            message = "회고가 성공적으로 작성되었습니다."
        )
    }

    /**
     * 회고 목록 조회 (최신순)
     */
    @Transactional(readOnly = true)
    fun getNotes(githubId: String): GetNotesResult {
        val userOpt = userRepository.findByGithubId(githubId)
        if (userOpt.isEmpty) {
            return GetNotesResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val notes = noteRepository.findByUserOrderByCreatedAtDesc(user)
        
        return GetNotesResult.Success(
            NoteListResponse(notes = notes.map { it.toNoteInfo() })
        )
    }

    /**
     * 회고 페이징 조회
     */
    @Transactional(readOnly = true)
    fun getNotesWithPaging(githubId: String, pageable: Pageable): GetNotesResult {
        val userOpt = userRepository.findByGithubId(githubId)
        if (userOpt.isEmpty) {
            return GetNotesResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val notesPage = noteRepository.findByUserOrderByCreatedAtDesc(user, pageable)
        
        return GetNotesResult.Success(
            NoteListResponse(
                notes = notesPage.content.map { it.toNoteInfo() },
                totalElements = notesPage.totalElements,
                totalPages = notesPage.totalPages,
                currentPage = notesPage.number,
                size = notesPage.size
            )
        )
    }

    /**
     * 회고 상세 조회
     */
    @Transactional(readOnly = true)
    fun getNote(githubId: String, noteId: Long): GetNoteResult {
        val userOpt = userRepository.findByGithubId(githubId)
        if (userOpt.isEmpty) {
            return GetNoteResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val noteOpt = noteRepository.findByIdAndUser(noteId, user)
        if (noteOpt.isEmpty) {
            return GetNoteResult.NotFound("회고를 찾을 수 없습니다: $noteId")
        }
        
        return GetNoteResult.Success(noteOpt.get().toNoteInfo())
    }

    /**
     * 회고 수정
     */
    fun updateNote(githubId: String, noteId: Long, request: UpdateNoteRequest): UpdateNoteResult {
        // 입력 값 검증
        val title = request.title.trim()
        val content = request.content.trim()
        
        if (title.isBlank()) {
            return UpdateNoteResult.ValidationError("제목은 필수입니다.")
        }
        if (content.isBlank()) {
            return UpdateNoteResult.ValidationError("내용은 필수입니다.")
        }
        
        // 사용자 조회
        val userOpt = userRepository.findByGithubId(githubId)
        if (userOpt.isEmpty) {
            return UpdateNoteResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        // 회고 조회
        val user = userOpt.get()
        val noteOpt = noteRepository.findByIdAndUser(noteId, user)
        if (noteOpt.isEmpty) {
            return UpdateNoteResult.NotFound("회고를 찾을 수 없습니다: $noteId")
        }
        
        val note = noteOpt.get()
        note.updateNote(title, content)
        val updatedNote = noteRepository.save(note)
        
        log.info("회고 수정 완료: 사용자={}, 제목={}, ID={}", githubId, title, noteId)
        
        return UpdateNoteResult.Success(
            note = updatedNote.toNoteInfo(),
            message = "회고가 성공적으로 수정되었습니다."
        )
    }

    /**
     * 회고 삭제
     */
    fun deleteNote(githubId: String, noteId: Long): DeleteNoteResult {
        val userOpt = userRepository.findByGithubId(githubId)
        if (userOpt.isEmpty) {
            return DeleteNoteResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val noteOpt = noteRepository.findByIdAndUser(noteId, user)
        if (noteOpt.isEmpty) {
            return DeleteNoteResult.NotFound("회고를 찾을 수 없습니다: $noteId")
        }
        
        noteRepository.delete(noteOpt.get())
        log.info("회고 삭제 완료: 사용자={}, ID={}", githubId, noteId)
        
        return DeleteNoteResult.Success("회고가 성공적으로 삭제되었습니다.")
    }

    /**
     * 회고 검색 (제목 또는 내용)
     */
    @Transactional(readOnly = true)
    fun searchNotes(githubId: String, keyword: String): SearchNotesResult {
        val userOpt = userRepository.findByGithubId(githubId)
        if (userOpt.isEmpty) {
            return SearchNotesResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val notes = noteRepository.findByUserAndTitleOrContentContaining(user, keyword.trim())
        
        return SearchNotesResult.Success(
            NoteSearchResponse(
                notes = notes.map { it.toNoteInfo() },
                totalCount = notes.size
            )
        )
    }

    /**
     * 사용자의 회고 개수 조회
     */
    @Transactional(readOnly = true)
    fun getNoteCount(githubId: String): GetNoteCountResult {
        val userOpt = userRepository.findByGithubId(githubId)
        if (userOpt.isEmpty) {
            return GetNoteCountResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val count = noteRepository.countByUser(user)
        
        return GetNoteCountResult.Success(NoteCountResponse(count))
    }
}

/**
 * Note Entity를 NoteInfo DTO로 변환하는 확장 함수
 */
private fun Note.toNoteInfo(): NoteInfo {
    return NoteInfo(
        id = this.id,
        title = this.title,
        content = this.content,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
} 