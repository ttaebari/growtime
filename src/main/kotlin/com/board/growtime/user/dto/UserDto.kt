package com.board.growtime.user.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 정보 데이터 전송 객체
 */
data class UserInfo(
    val id: Int?,
    val githubId: String,
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val htmlUrl: String?,
    val location: String?,
    val entryDate: LocalDate?,
    val dischargeDate: LocalDate?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) 