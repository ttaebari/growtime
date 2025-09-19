package com.board.growtime.user.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * D-day 정보 데이터 전송 객체
 */
data class DDayInfo(
    @JsonProperty("dDay")
    val dDay: Long,
    val serviceDays: Long,
    val totalServiceDays: Long,
    val entryDate: LocalDate,
    val dischargeDate: LocalDate,
    val progressPercentage: Double
)
