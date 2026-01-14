package com.board.growtime.note

import com.board.growtime.common.exception.NoteNotFoundException
import com.board.growtime.common.exception.InvalidNoteDataException
import com.board.growtime.common.exception.UserNotFoundException
import com.board.growtime.common.util.ValidationUtils
import com.board.growtime.note.dto.*
import com.board.growtime.note.dto.toNoteInfo
import com.board.growtime.note.dto.toNoteInfoList
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
class NoteService(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(NoteService::class.java)

    /**
     * 회고 작성
     */
    @Transactional
    fun createNote(githubId: String, request: CreateNoteRequest): NoteInfo {
        validateGitHubId(githubId)
        validateNoteData(request)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: run {
                log.warn("회고 작성 실패 - 사용자 없음: {}", githubId)
                throw UserNotFoundException(githubId)
            }
        
        val note = Note(
            title = request.title.trim(),
            content = request.content.trim(),
            user = user,
            developType = request.developType
        )
        val savedNote = noteRepository.save(note)
        
        log.info("회고 작성 완료: 사용자={}, 제목={}, ID={}", githubId, request.title, savedNote.id)
        
        return savedNote.toNoteInfo()
    }

    /**
     * 회고 목록 조회
     */
    @Transactional(readOnly = true)
    fun getNotes(githubId: String): List<NoteInfo> {
        validateGitHubId(githubId)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: throw UserNotFoundException(githubId)
        
        val notes = noteRepository.findByUserOrderByCreatedAtDesc(user)
        
        return notes.toNoteInfoList()
    }

    /**
     * 회고 페이징 조회
     */
    @Transactional(readOnly = true)
    fun getNotesWithPaging(githubId: String, pageable: Pageable): NoteListResponse {
        validateGitHubId(githubId)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: throw UserNotFoundException(githubId)
        
        val notesPage = noteRepository.findByUserOrderByCreatedAtDesc(user, pageable)
        
        return NoteListResponse(
            notes = notesPage.content.toNoteInfoList(),
            totalElements = notesPage.totalElements,
            totalPages = notesPage.totalPages,
            currentPage = notesPage.number,
            size = notesPage.size
        )
    }

    /**
     * 회고 상세 조회
     */
    @Transactional(readOnly = true)
    fun getNote(githubId: String, noteId: Long): NoteInfo {
        validateGitHubId(githubId)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: throw UserNotFoundException(githubId)
        
        val note = noteRepository.findByIdAndUser(noteId, user)
            ?: run {
                log.warn("회고 조회 실패 - 회고 없음: 사용자={}, 회고ID={}", githubId, noteId)
                throw NoteNotFoundException(noteId)
            }
        
        return note.toNoteInfo()
    }

    /**
     * 회고 수정
     */
    @Transactional
    fun updateNote(githubId: String, noteId: Long, request: UpdateNoteRequest): NoteInfo {
        validateGitHubId(githubId)
        validateNoteData(request)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: throw UserNotFoundException(githubId)
        
        val note = noteRepository.findByIdAndUser(noteId, user)
            ?: throw NoteNotFoundException(noteId)
        
        note.updateNote(
            title = request.title.trim(),
            content = request.content.trim(),
            developType = request.developType
        )
        val updatedNote = noteRepository.save(note)
        
        log.info("회고 수정 완료: 사용자={}, 제목={}, ID={}", githubId, request.title, noteId)
        
        return updatedNote.toNoteInfo()
    }

    /**
     * 회고 삭제
     */
    @Transactional
    fun deleteNote(githubId: String, noteId: Long) {
        validateGitHubId(githubId)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: throw UserNotFoundException(githubId)
        
        val note = noteRepository.findByIdAndUser(noteId, user)
            ?: throw NoteNotFoundException(noteId)
        
        noteRepository.delete(note)
        log.info("회고 삭제 완료: 사용자={}, ID={}", githubId, noteId)
    }

    /**
     * 회고 검색
     */
    @Transactional(readOnly = true)
    fun searchNotes(githubId: String, keyword: String): NoteSearchResponse {
        validateGitHubId(githubId)
        validateSearchKeyword(keyword)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: throw UserNotFoundException(githubId)
        
        val notes = noteRepository.findByUserAndTitleOrContentContaining(user, keyword.trim())
        
        return NoteSearchResponse(
            notes = notes.toNoteInfoList(),
            totalCount = notes.size
        )
    }

    /**
     * 사용자의 회고 개수 조회
     */
    @Transactional(readOnly = true)
    fun getNoteCount(githubId: String): NoteCountResponse {
        validateGitHubId(githubId)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: throw UserNotFoundException(githubId)
        
        val count = noteRepository.countByUser(user)
        
        return NoteCountResponse(count)
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
     * 회고 데이터 검증
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
     * 회고 데이터 검증 (Update용)
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