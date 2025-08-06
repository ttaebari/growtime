package com.board.growtime.note

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val noteService: NoteService
) {
    private val log = LoggerFactory.getLogger(NoteController::class.java)

    /**
     * 회고 작성
     */
    @PostMapping("/{githubId}")
    fun createNote(
        @PathVariable githubId: String,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val result = noteService.createNote(githubId, request)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            log.error("회고 작성 실패: {}", e.message)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 작성 중 오류 발생", e)
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 회고 목록 조회
     */
    @GetMapping("/{githubId}")
    fun getNotes(
        @PathVariable githubId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val pageable = PageRequest.of(page, size)
            val result = noteService.getNotesWithPaging(githubId, pageable)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            log.error("회고 목록 조회 실패: {}", e.message)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 목록 조회 중 오류 발생", e)
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 회고 상세 조회
     */
    @GetMapping("/{githubId}/{noteId}")
    fun getNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val result = noteService.getNote(githubId, noteId)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            log.error("회고 상세 조회 실패: {}", e.message)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 상세 조회 중 오류 발생", e)
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 회고 수정
     */
    @PutMapping("/{githubId}/{noteId}")
    fun updateNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val result = noteService.updateNote(githubId, noteId, request)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            log.error("회고 수정 실패: {}", e.message)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 수정 중 오류 발생", e)
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 회고 삭제
     */
    @DeleteMapping("/{githubId}/{noteId}")
    fun deleteNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val result = noteService.deleteNote(githubId, noteId)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            log.error("회고 삭제 실패: {}", e.message)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 삭제 중 오류 발생", e)
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 회고 검색
     */
    @GetMapping("/{githubId}/search")
    fun searchNotes(
        @PathVariable githubId: String,
        @RequestParam keyword: String
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val result = noteService.searchNotes(githubId, keyword)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            log.error("회고 검색 실패: {}", e.message)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 검색 중 오류 발생", e)
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 회고 개수 조회
     */
    @GetMapping("/{githubId}/count")
    fun getNoteCount(@PathVariable githubId: String): ResponseEntity<Map<String, Any>> {
        return try {
            val result = noteService.getNoteCount(githubId)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            log.error("회고 개수 조회 실패: {}", e.message)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 개수 조회 중 오류 발생", e)
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }
} 