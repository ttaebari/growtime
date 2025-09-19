package com.board.growtime.note.dto

import com.board.growtime.enums.developType
import java.time.LocalDateTime
import java.util.*

/**
 * 회고 정보 데이터 전송 객체
 */
data class NoteInfo(
    val id: Int?,
    val title: String,
    val content: String,
    val developType: developType,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

/**
 * 회고 생성 요청 DTO
 */
data class CreateNoteRequest(
    val title: String,
    val content: String,
    val developType: developType
)

/**
 * 회고 수정 요청 DTO
 */
data class UpdateNoteRequest(
    val title: String,
    val content: String,
    val developType: developType
)

/**
 * 회고 목록 응답 DTO
 */
data class NoteListResponse(
    val notes: List<NoteInfo>,
    val totalElements: Long? = null,
    val totalPages: Int? = null,
    val currentPage: Int? = null,
    val size: Int? = null
)

/**
 * 회고 검색 응답 DTO
 */
data class NoteSearchResponse(
    val notes: List<NoteInfo>,
    val totalCount: Int
)

/**
 * 회고 개수 응답 DTO
 */
data class NoteCountResponse(
    val count: Long
) 