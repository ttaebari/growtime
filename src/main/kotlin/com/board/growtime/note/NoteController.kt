package com.board.growtime.note

import com.board.growtime.note.dto.*
import com.board.growtime.note.result.*
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
        @RequestBody request: CreateNoteRequest
    ): ResponseEntity<*> {
        return when (val result = noteService.createNote(githubId, request)) {
            is CreateNoteResult.Success -> {
                val response = mapOf(
                    "message" to result.message,
                    "note" to result.note
                )
                ResponseEntity.ok(response)
            }
            is CreateNoteResult.ValidationError -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
            is CreateNoteResult.UserNotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
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
    ): ResponseEntity<*> {
        val pageable = PageRequest.of(page, size)
        return when (val result = noteService.getNotesWithPaging(githubId, pageable)) {
            is GetNotesResult.Success -> ResponseEntity.ok(result.response)
            is GetNotesResult.UserNotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
        }
    }

    /**
     * 회고 상세 조회
     */
    @GetMapping("/{githubId}/{noteId}")
    fun getNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long
    ): ResponseEntity<*> {
        return when (val result = noteService.getNote(githubId, noteId)) {
            is GetNoteResult.Success -> {
                val response = mapOf("note" to result.note)
                ResponseEntity.ok(response)
            }
            is GetNoteResult.NotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
            is GetNoteResult.UserNotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
        }
    }

    /**
     * 회고 수정
     */
    @PutMapping("/{githubId}/{noteId}")
    fun updateNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long,
        @RequestBody request: UpdateNoteRequest
    ): ResponseEntity<*> {
        return when (val result = noteService.updateNote(githubId, noteId, request)) {
            is UpdateNoteResult.Success -> {
                val response = mapOf(
                    "message" to result.message,
                    "note" to result.note
                )
                ResponseEntity.ok(response)
            }
            is UpdateNoteResult.ValidationError -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
            is UpdateNoteResult.NotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
            is UpdateNoteResult.UserNotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
        }
    }

    /**
     * 회고 삭제
     */
    @DeleteMapping("/{githubId}/{noteId}")
    fun deleteNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long
    ): ResponseEntity<*> {
        return when (val result = noteService.deleteNote(githubId, noteId)) {
            is DeleteNoteResult.Success -> {
                val response = mapOf("message" to result.message)
                ResponseEntity.ok(response)
            }
            is DeleteNoteResult.NotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
            is DeleteNoteResult.UserNotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
        }
    }

    /**
     * 회고 검색
     */
    @GetMapping("/{githubId}/search")
    fun searchNotes(
        @PathVariable githubId: String,
        @RequestParam keyword: String
    ): ResponseEntity<*> {
        return when (val result = noteService.searchNotes(githubId, keyword)) {
            is SearchNotesResult.Success -> ResponseEntity.ok(result.response)
            is SearchNotesResult.UserNotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
        }
    }

    /**
     * 회고 개수 조회
     */
    @GetMapping("/{githubId}/count")
    fun getNoteCount(@PathVariable githubId: String): ResponseEntity<*> {
        return when (val result = noteService.getNoteCount(githubId)) {
            is GetNoteCountResult.Success -> ResponseEntity.ok(result.response)
            is GetNoteCountResult.UserNotFound -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
        }
    }
} 