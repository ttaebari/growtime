package com.board.growtime.quicklink.dto

import com.board.growtime.quicklink.QuickLink

fun QuickLink.toResponse() = QuickLinkResponse(
    id = this.id!!,
    title = this.title,
    url = this.url,
    faviconUrl = this.faviconUrl
)
