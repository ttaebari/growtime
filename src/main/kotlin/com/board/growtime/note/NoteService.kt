package com.board.growtime.note

import com.board.growtime.core.exception.NoteNotFoundException
import com.board.growtime.core.exception.InvalidNoteDataException
import com.board.growtime.core.exception.UserNotFoundException
import com.board.growtime.core.util.ValidationUtils
import com.board.growtime.note.dto.*
import com.board.growtime.note.result.*
import com.board.growtime.user.User
import com.board.growtime.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회고 관련 비즈니스 로직을 담당하는 서비스
 * 
 * 학습 포인트:
 * 1. Service Layer 패턴 - 회고 관련 비즈니스 로직 캡슐화
 * 2. 일관된 서비스 구조 - 다른 서비스와 동일한 패턴 적용
 * 3. 검증 로직 분리 - 유틸리티 클래스를 활용한 입력값 검증
 * 4. 예외 처리 통일 - 명확한 예외 타입으로 에러 상황 구분
 */
@Service
@Transactional
class NoteService(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(NoteService::class.java)

    /**
     * 회고 작성 (비즈니스 로직 포함)
     * 
     * 학습 포인트:
     * - 입력값 검증을 서비스 레이어에서 담당
     * - 비즈니스 규칙을 명확히 정의하고 적용
     * - 로깅을 통한 중요한 비즈니스 이벤트 추적
     */
    fun createNote(githubId: String, request: CreateNoteRequest): CreateNoteResult {
        // 비즈니스 로직: 입력값 검증
        validateGitHubId(githubId)
        validateNoteData(request)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
        if (userOpt.isEmpty) {
            log.warn("회고 작성 실패 - 사용자 없음: {}", githubId)
            return CreateNoteResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val note = Note(
            title = request.title.trim(),
            content = request.content.trim(),
            user = user,
            developType = request.developType
        )
        val savedNote = noteRepository.save(note)
        
        log.info("회고 작성 완료: 사용자={}, 제목={}, ID={}", githubId, request.title, savedNote.id)
        
        return CreateNoteResult.Success(
            note = savedNote.toNoteInfo(),
            message = "회고가 성공적으로 작성되었습니다."
        )
    }

    /**
     * 회고 목록 조회 (비즈니스 로직 포함)
     */
    @Transactional(readOnly = true)
    fun getNotes(githubId: String): GetNotesResult {
        validateGitHubId(githubId)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
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
     * 회고 페이징 조회 (비즈니스 로직 포함)
     */
    @Transactional(readOnly = true)
    fun getNotesWithPaging(githubId: String, pageable: Pageable): GetNotesResult {
        validateGitHubId(githubId)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
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
     * 회고 상세 조회 (비즈니스 로직 포함)
     */
    @Transactional(readOnly = true)
    fun getNote(githubId: String, noteId: Long): GetNoteResult {
        validateGitHubId(githubId)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
        if (userOpt.isEmpty) {
            return GetNoteResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val noteOpt = noteRepository.findByIdAndUser(noteId, user)
        if (noteOpt.isEmpty) {
            log.warn("회고 조회 실패 - 회고 없음: 사용자={}, 회고ID={}", githubId, noteId)
            return GetNoteResult.NotFound("회고를 찾을 수 없습니다: $noteId")
        }
        
        return GetNoteResult.Success(noteOpt.get().toNoteInfo())
    }

    /**
     * 회고 수정 (비즈니스 로직 포함)
     */
    fun updateNote(githubId: String, noteId: Long, request: UpdateNoteRequest): UpdateNoteResult {
        // 비즈니스 로직: 입력값 검증
        validateGitHubId(githubId)
        validateNoteData(request)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
        if (userOpt.isEmpty) {
            return UpdateNoteResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val noteOpt = noteRepository.findByIdAndUser(noteId, user)
        if (noteOpt.isEmpty) {
            return UpdateNoteResult.NotFound("회고를 찾을 수 없습니다: $noteId")
        }
        
        val note = noteOpt.get()
        note.updateNote(
            title = request.title.trim(),
            content = request.content.trim(),
            developType = request.developType
        )
        val updatedNote = noteRepository.save(note)
        
        log.info("회고 수정 완료: 사용자={}, 제목={}, ID={}", githubId, request.title, noteId)
        
        return UpdateNoteResult.Success(
            note = updatedNote.toNoteInfo(),
            message = "회고가 성공적으로 수정되었습니다."
        )
    }

    /**
     * 회고 삭제 (비즈니스 로직 포함)
     */
    fun deleteNote(githubId: String, noteId: Long): DeleteNoteResult {
        validateGitHubId(githubId)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
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
     * 회고 검색 (비즈니스 로직 포함)
     */
    @Transactional(readOnly = true)
    fun searchNotes(githubId: String, keyword: String): SearchNotesResult {
        validateGitHubId(githubId)
        validateSearchKeyword(keyword)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
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
     * 사용자의 회고 개수 조회 (비즈니스 로직 포함)
     */
    @Transactional(readOnly = true)
    fun getNoteCount(githubId: String): GetNoteCountResult {
        validateGitHubId(githubId)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
        if (userOpt.isEmpty) {
            return GetNoteCountResult.UserNotFound("사용자를 찾을 수 없습니다: $githubId")
        }
        
        val user = userOpt.get()
        val count = noteRepository.countByUser(user)
        
        return GetNoteCountResult.Success(NoteCountResponse(count))
    }

    /**
     * GitHub ID 검증
     */
    private fun validateGitHubId(githubId: String) {
        if (!ValidationUtils.isValidGitHubId(githubId)) {
            throw InvalidNoteDataException("올바른 GitHub ID 형식이 아닙니다: $githubId")
        }
    }

    /**
     * 회고 데이터 검증 (비즈니스 규칙)
     */
    private fun validateNoteData(data: CreateNoteRequest) {
        if (!ValidationUtils.isValidNoteTitle(data.title)) {
            throw InvalidNoteDataException("제목은 1-100자 사이로 입력해주세요.")
        }
        
        if (!ValidationUtils.isValidNoteContent(data.content)) {
            throw InvalidNoteDataException("내용은 1-5000자 사이로 입력해주세요.")
        }
    }

    /**
     * 회고 데이터 검증 (비즈니스 규칙) - Update용
     */
    private fun validateNoteData(data: UpdateNoteRequest) {
        if (!ValidationUtils.isValidNoteTitle(data.title)) {
            throw InvalidNoteDataException("제목은 1-100자 사이로 입력해주세요.")
        }
        
        if (!ValidationUtils.isValidNoteContent(data.content)) {
            throw InvalidNoteDataException("내용은 1-5000자 사이로 입력해주세요.")
        }
    }

    /**
     * 검색 키워드 검증
     */
    private fun validateSearchKeyword(keyword: String) {
        if (ValidationUtils.isBlank(keyword)) {
            throw InvalidNoteDataException("검색 키워드를 입력해주세요.")
        }
        
        if (keyword.trim().length < 2) {
            throw InvalidNoteDataException("검색 키워드는 최소 2글자 이상이어야 합니다.")
        }
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
        developType = this.developType,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
} 