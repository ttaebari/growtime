package com.board.growtime.user.dto

import java.time.LocalDate

data class ServiceDatesRequest(
    val entryDate: LocalDate,
    val dischargeDate: LocalDate
)
