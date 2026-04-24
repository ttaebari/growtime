package com.board.growtime.auth

import com.board.growtime.common.response.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Component
class ApiAuthenticationInterceptor(
    private val jwtService: JwtService,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (request.method == HttpMethod.OPTIONS.name()) {
            return true
        }

        val token = extractBearerToken(request)
            ?: return writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다.")

        val authenticatedUser = try {
            jwtService.parseToken(token)
        } catch (e: JwtAuthenticationException) {
            return writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "유효하지 않은 인증 토큰입니다.")
        }

        val pathGithubId = extractPathGithubId(request)
        if (pathGithubId != null && pathGithubId != authenticatedUser.githubId) {
            return writeError(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "다른 사용자의 리소스에 접근할 수 없습니다.")
        }

        request.setAttribute(AUTHENTICATED_GITHUB_ID_ATTRIBUTE, authenticatedUser.githubId)
        request.setAttribute(AUTHENTICATED_LOGIN_ATTRIBUTE, authenticatedUser.login)
        return true
    }

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)?.trim()
            ?: return null
        if (!authorization.startsWith("Bearer ", ignoreCase = true)) {
            return null
        }
        return authorization.substring("Bearer ".length).trim().takeIf { it.isNotBlank() }
    }

    private fun extractPathGithubId(request: HttpServletRequest): String? {
        val path = request.requestURI.removePrefix(request.contextPath.orEmpty())
        val segments = path.split("/").filter { it.isNotBlank() }

        if (segments.size < 3 || segments[0] != "api") {
            return null
        }

        if (segments[1] != "user" && segments[1] != "notes") {
            return null
        }

        return URLDecoder.decode(segments[2], StandardCharsets.UTF_8)
    }

    private fun writeError(
        response: HttpServletResponse,
        status: HttpStatus,
        code: String,
        message: String
    ): Boolean {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        objectMapper.writeValue(response.writer, ApiResponse.error<Any>(code, message))
        return false
    }

    companion object {
        const val AUTHENTICATED_GITHUB_ID_ATTRIBUTE = "authenticatedGithubId"
        const val AUTHENTICATED_LOGIN_ATTRIBUTE = "authenticatedLogin"
    }
}
