package com.board.growtime.quicklink.dto

import com.board.growtime.quicklink.QuickLink

data class QuickLinkRequest(
    val title: String,
    val url: String
)

data class QuickLinkResponse(
    val id: Long,
    val title: String,
    val url: String,
    val faviconUrl: String?
)

fun QuickLink.toResponse() = QuickLinkResponse(
    id = this.id!!,
    title = this.title,
    url = this.url,
    faviconUrl = this.faviconUrl
)
