package com.board.growtime.user.dto

import java.time.LocalDate

data class ServiceDatesRequest(
    val entryDate: LocalDate,
    val dischargeDate: LocalDate
)

data class GitHubUserRequest(
    val githubId: String,
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val htmlUrl: String?,
    val location: String?
)
