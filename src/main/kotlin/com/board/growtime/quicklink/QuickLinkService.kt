package com.board.growtime.quicklink

import com.board.growtime.quicklink.dto.QuickLinkRequest
import com.board.growtime.quicklink.dto.QuickLinkResponse
import com.board.growtime.quicklink.dto.toResponse
import com.board.growtime.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
@Transactional(readOnly = true)
class QuickLinkService(
    private val quickLinkRepository: QuickLinkRepository,
    private val userRepository: UserRepository
) {

    fun getQuickLinks(githubId: String): List<QuickLinkResponse> {
        return quickLinkRepository.findAllByUser_GithubIdOrderByCreatedAtAsc(githubId)
            .map { it.toResponse() }
    }

    @Transactional
    fun createQuickLink(githubId: String, request: QuickLinkRequest): QuickLinkResponse {
        val user = userRepository.findByGithubId(githubId)
            ?: throw IllegalArgumentException("User not found with githubId: $githubId")

        // URL에서 도메인 추출하여 파비콘 URL 생성
        val faviconUrl = try {
            val domain = URI(request.url).host
            "https://www.google.com/s2/favicons?domain=$domain&sz=64"
        } catch (e: Exception) {
            null
        }

        val quickLink = QuickLink(
            user = user,
            title = request.title,
            url = request.url,
            faviconUrl = faviconUrl
        )

        val savedLink = quickLinkRepository.save(quickLink)
        return savedLink.toResponse()
    }

    @Transactional
    fun deleteQuickLink(githubId: String, linkId: Long) {
        val quickLink = quickLinkRepository.findById(linkId)
            .orElseThrow { IllegalArgumentException("QuickLink not found with id: $linkId") }

        if (quickLink.user.githubId != githubId) {
            throw IllegalArgumentException("Unauthorized to delete this link")
        }

        quickLinkRepository.delete(quickLink)
    }
}