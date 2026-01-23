package com.board.growtime.quicklink.dto



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

