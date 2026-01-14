package com.board.growtime.note

import com.board.growtime.common.response.ApiResponse
import com.board.growtime.note.dto.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
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
    ): ApiResponse<NoteInfo> {
        val note = noteService.createNote(githubId, request)
        return ApiResponse.success(note)
    }

    /**
     * 회고 목록 조회
     */
    @GetMapping("/{githubId}")
    fun getNotes(
        @PathVariable githubId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<NoteListResponse> {
        val pageable = PageRequest.of(page, size)
        val result = noteService.getNotesWithPaging(githubId, pageable)
        return ApiResponse.success(result)
    }

    /**
     * 회고 상세 조회
     */
    @GetMapping("/{githubId}/{noteId}")
    fun getNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long
    ): ApiResponse<NoteInfo> {
        val note = noteService.getNote(githubId, noteId)
        return ApiResponse.success(note)
    }

    /**
     * 회고 수정
     */
    @PutMapping("/{githubId}/{noteId}")
    fun updateNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long,
        @RequestBody request: UpdateNoteRequest
    ): ApiResponse<NoteInfo> {
        val note = noteService.updateNote(githubId, noteId, request)
        return ApiResponse.success(note)
    }

    /**
     * 회고 삭제
     */
    @DeleteMapping("/{githubId}/{noteId}")
    fun deleteNote(
        @PathVariable githubId: String,
        @PathVariable noteId: Long
    ): ApiResponse<Nothing> {
        noteService.deleteNote(githubId, noteId)
        return ApiResponse.success()
    }

    /**
     * 회고 검색
     */
    @GetMapping("/{githubId}/search")
    fun searchNotes(
        @PathVariable githubId: String,
        @RequestParam keyword: String
    ): ApiResponse<NoteSearchResponse> {
        val result = noteService.searchNotes(githubId, keyword)
        return ApiResponse.success(result)
    }

    /**
     * 회고 개수 조회
     */
    @GetMapping("/{githubId}/count")
    fun getNoteCount(@PathVariable githubId: String): ApiResponse<NoteCountResponse> {
        val result = noteService.getNoteCount(githubId)
        return ApiResponse.success(result)
    }
} 