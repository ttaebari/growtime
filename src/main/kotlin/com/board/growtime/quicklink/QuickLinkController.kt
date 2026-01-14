package com.board.growtime.quicklink

import com.board.growtime.common.response.ApiResponse
import com.board.growtime.quicklink.dto.QuickLinkRequest
import com.board.growtime.quicklink.dto.QuickLinkResponse
import com.board.growtime.quicklink.QuickLinkService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/{githubId}/quick-links")
class QuickLinkController(
    private val quickLinkService: QuickLinkService
) {

    @GetMapping
    fun getQuickLinks(@PathVariable githubId: String): ApiResponse<List<QuickLinkResponse>> {
        val links = quickLinkService.getQuickLinks(githubId)
        return ApiResponse.Companion.success(links)
    }

    @PostMapping
    fun createQuickLink(
        @PathVariable githubId: String,
        @RequestBody request: QuickLinkRequest
    ): ApiResponse<QuickLinkResponse> {
        val link = quickLinkService.createQuickLink(githubId, request)
        return ApiResponse.Companion.success(link)
    }

    @DeleteMapping("/{linkId}")
    fun deleteQuickLink(
        @PathVariable githubId: String,
        @PathVariable linkId: Long
    ): ApiResponse<Unit> {
        quickLinkService.deleteQuickLink(githubId, linkId)
        return ApiResponse.Companion.success(Unit)
    }
}