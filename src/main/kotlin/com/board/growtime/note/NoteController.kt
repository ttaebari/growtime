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
        try {
            val title = request["title"]
            val content = request["content"]
            
            if (title.isNullOrBlank()) {
                return ResponseEntity.badRequest().body(mapOf("error" to "제목은 필수입니다."))
            }
            if (content.isNullOrBlank()) {
                return ResponseEntity.badRequest().body(mapOf("error" to "내용은 필수입니다."))
            }
            
            val note = noteService.createNote(githubId, title, content)
            
            val response = mapOf(
                "message" to "회고가 성공적으로 작성되었습니다.",
                "note" to mapOf(
                    "id" to note.id,
                    "title" to note.title,
                    "content" to note.content,
                    "createdAt" to note.createdAt
                )
            )
            
            return ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            log.error("회고 작성 실패: {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 작성 중 오류 발생", e)
            return ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
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
        try {
            val pageable = PageRequest.of(page, size)
            val notesPage = noteService.getNotesWithPaging(githubId, pageable)
            
            val response = mapOf(
                "notes" to notesPage.content.map { note ->
                    mapOf(
                        "id" to note.id,
                        "title" to note.title,
                        "content" to note.content,
                        "createdAt" to note.createdAt,
                        "updatedAt" to note.updatedAt
                    )
                },
                "totalElements" to notesPage.totalElements,
                "totalPages" to notesPage.totalPages,
                "currentPage" to notesPage.number,
                "size" to notesPage.size
            )
            
            return ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            log.error("회고 목록 조회 실패: {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 목록 조회 중 오류 발생", e)
            return ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
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
        try {
            val note = noteService.getNote(githubId, noteId)
            
            val response = mapOf(
                "note" to mapOf(
                    "id" to note.id,
                    "title" to note.title,
                    "content" to note.content,
                    "createdAt" to note.createdAt,
                    "updatedAt" to note.updatedAt
                )
            )
            
            return ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            log.error("회고 상세 조회 실패: {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 상세 조회 중 오류 발생", e)
            return ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
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
        try {
            val title = request["title"]
            val content = request["content"]
            
            if (title.isNullOrBlank()) {
                return ResponseEntity.badRequest().body(mapOf("error" to "제목은 필수입니다."))
            }
            if (content.isNullOrBlank()) {
                return ResponseEntity.badRequest().body(mapOf("error" to "내용은 필수입니다."))
            }
            
            val note = noteService.updateNote(githubId, noteId, title, content)
            
            val response = mapOf(
                "message" to "회고가 성공적으로 수정되었습니다.",
                "note" to mapOf(
                    "id" to note.id,
                    "title" to note.title,
                    "content" to note.content,
                    "updatedAt" to note.updatedAt
                )
            )
            
            return ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            log.error("회고 수정 실패: {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 수정 중 오류 발생", e)
            return ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
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
        try {
            noteService.deleteNote(githubId, noteId)
            
            val response = mapOf("message" to "회고가 성공적으로 삭제되었습니다.")
            
            return ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            log.error("회고 삭제 실패: {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 삭제 중 오류 발생", e)
            return ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
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
        try {
            val notes = noteService.searchNotes(githubId, keyword)
            
            val response = mapOf(
                "notes" to notes.map { note ->
                    mapOf(
                        "id" to note.id,
                        "title" to note.title,
                        "content" to note.content,
                        "createdAt" to note.createdAt
                    )
                },
                "totalCount" to notes.size
            )
            
            return ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            log.error("회고 검색 실패: {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 검색 중 오류 발생", e)
            return ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 회고 개수 조회
     */
    @GetMapping("/{githubId}/count")
    fun getNoteCount(@PathVariable githubId: String): ResponseEntity<Map<String, Any>> {
        try {
            val count = noteService.getNoteCount(githubId)
            
            val response = mapOf("count" to count)
            
            return ResponseEntity.ok(response)
            
        } catch (e: IllegalArgumentException) {
            log.error("회고 개수 조회 실패: {}", e.message)
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "오류 발생")))
        } catch (e: Exception) {
            log.error("회고 개수 조회 중 오류 발생", e)
            return ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }
} 