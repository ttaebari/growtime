package com.board.growtime.note.result

import com.board.growtime.note.dto.*

/**
 * 회고 생성 결과
 */
sealed class CreateNoteResult {
    data class Success(val note: NoteInfo, val message: String) : CreateNoteResult()
    data class ValidationError(val message: String) : CreateNoteResult()
    data class UserNotFound(val message: String) : CreateNoteResult()
}

/**
 * 회고 조회 결과
 */
sealed class GetNoteResult {
    data class Success(val note: NoteInfo) : GetNoteResult()
    data class NotFound(val message: String) : GetNoteResult()
    data class UserNotFound(val message: String) : GetNoteResult()
}

/**
 * 회고 목록 조회 결과
 */
sealed class GetNotesResult {
    data class Success(val response: NoteListResponse) : GetNotesResult()
    data class UserNotFound(val message: String) : GetNotesResult()
}

/**
 * 회고 수정 결과
 */
sealed class UpdateNoteResult {
    data class Success(val note: NoteInfo, val message: String) : UpdateNoteResult()
    data class ValidationError(val message: String) : UpdateNoteResult()
    data class NotFound(val message: String) : UpdateNoteResult()
    data class UserNotFound(val message: String) : UpdateNoteResult()
}

/**
 * 회고 삭제 결과
 */
sealed class DeleteNoteResult {
    data class Success(val message: String) : DeleteNoteResult()
    data class NotFound(val message: String) : DeleteNoteResult()
    data class UserNotFound(val message: String) : DeleteNoteResult()
}

/**
 * 회고 검색 결과
 */
sealed class SearchNotesResult {
    data class Success(val response: NoteSearchResponse) : SearchNotesResult()
    data class UserNotFound(val message: String) : SearchNotesResult()
}

/**
 * 회고 개수 조회 결과
 */
sealed class GetNoteCountResult {
    data class Success(val response: NoteCountResponse) : GetNoteCountResult()
    data class UserNotFound(val message: String) : GetNoteCountResult()
} 