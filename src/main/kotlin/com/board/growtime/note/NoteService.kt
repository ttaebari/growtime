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
    fun createNote(githubId: String, title: String, content: String): Note {
        val user = userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
        
        val note = Note(title, content, user)
        val savedNote = noteRepository.save(note)
        
        log.info("회고 작성 완료: 사용자={}, 제목={}, ID={}", githubId, title, savedNote.id)
        return savedNote
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
    fun getNotesWithPaging(githubId: String, pageable: Pageable): Page<Note> {
        val user = userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
        
        return noteRepository.findByUserOrderByCreatedAtDesc(user, pageable)
    }

    /**
     * 회고 상세 조회
     */
    @Transactional(readOnly = true)
    fun getNote(githubId: String, noteId: Long): Note {
        val user = userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
        
        return noteRepository.findByIdAndUser(noteId, user)
            .orElseThrow { IllegalArgumentException("회고를 찾을 수 없습니다: $noteId") }
    }

    /**
     * 회고 수정
     */
    fun updateNote(githubId: String, noteId: Long, title: String, content: String): Note {
        val user = userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
        
        val note = noteRepository.findByIdAndUser(noteId, user)
            .orElseThrow { IllegalArgumentException("회고를 찾을 수 없습니다: $noteId") }
        
        note.updateNote(title, content)
        val updatedNote = noteRepository.save(note)
        
        log.info("회고 수정 완료: 사용자={}, 제목={}, ID={}", githubId, title, noteId)
        return updatedNote
    }

    /**
     * 회고 삭제
     */
    fun deleteNote(githubId: String, noteId: Long) {
        val user = userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
        
        val note = noteRepository.findByIdAndUser(noteId, user)
            .orElseThrow { IllegalArgumentException("회고를 찾을 수 없습니다: $noteId") }
        
        noteRepository.delete(note)
        log.info("회고 삭제 완료: 사용자={}, ID={}", githubId, noteId)
    }

    /**
     * 회고 검색 (제목 또는 내용)
     */
    @Transactional(readOnly = true)
    fun searchNotes(githubId: String, keyword: String): List<Note> {
        val user = userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
        
        return noteRepository.findByUserAndTitleOrContentContaining(user, keyword)
    }

    /**
     * 사용자의 회고 개수 조회
     */
    @Transactional(readOnly = true)
    fun getNoteCount(githubId: String): Long {
        val user = userRepository.findByGithubId(githubId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $githubId") }
        
        return noteRepository.countByUser(user)
    }
} 