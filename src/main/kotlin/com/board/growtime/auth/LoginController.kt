package com.board.growtime.auth

import com.board.growtime.auth.dto.GitHubUserInfo
import com.board.growtime.auth.dto.LoginResponse
import com.board.growtime.auth.dto.OAuthAccessTokenRequest
import com.board.growtime.auth.dto.OAuthAccessTokenResponse
import com.board.growtime.common.response.ApiResponse
import com.board.growtime.user.GitHubUserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@RestController
class LoginController(
    private val restTemplate: RestTemplate,
    private val gitHubUserService: GitHubUserService,
    private val jwtService: JwtService,
    @Value("\${github.clientId}")
    private val clientId: String,
    @Value("\${github.clientSecret}")
    private val clientSecret: String
) {
    private val log = LoggerFactory.getLogger(LoginController::class.java)

    companion object {
        private const val ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token"
        private const val MEMBER_INFO_URL = "https://api.github.com/user"
    }

    @GetMapping("/callback")
    fun handleCallback(
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) error: String?
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        if (error != null) {
            log.warn("GitHub OAuth error: {}", error)
            return loginError(HttpStatus.BAD_REQUEST, "OAUTH_ERROR", error)
        }

        if (code.isNullOrBlank()) {
            log.warn("GitHub OAuth callback missing code")
            return loginError(HttpStatus.BAD_REQUEST, "NO_AUTH_CODE", "인증 코드가 없습니다")
        }

        try {
            val githubAccessToken = getAccessToken(code)
                ?: return loginError(HttpStatus.BAD_GATEWAY, "GITHUB_TOKEN_EXCHANGE_FAILED", "GitHub 인증 처리에 실패했습니다")
            
            val userInfo = getUserInfo(githubAccessToken)
                ?: return loginError(HttpStatus.BAD_GATEWAY, "GITHUB_USER_INFO_FAILED", "GitHub 사용자 정보를 받지 못했습니다")
            
            val savedUser = gitHubUserService.saveOrUpdateUser(
                userInfo.id.toString(),
                userInfo.login,
                userInfo.name,
                userInfo.avatarUrl,
                userInfo.htmlUrl,
                userInfo.location
            )
            log.info("GitHub login succeeded: githubId={}, login={}", savedUser.githubId, savedUser.login)

            val appToken = jwtService.createToken(savedUser.githubId, savedUser.login)
            val response = LoginResponse(
                accessToken = appToken,
                refreshToken = null,
                githubId = savedUser.githubId
            )
            return ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: Exception) {
            log.error("GitHub OAuth callback processing failed", e)
            throw e // Let GlobalExceptionHandler handle it
        }
    }

    private fun getAccessToken(code: String): String? {
        return try {
            val headers = HttpHeaders().apply {
                set("Accept", "application/json")
                contentType = MediaType.APPLICATION_JSON
            }

            val tokenRequest = OAuthAccessTokenRequest(clientId, clientSecret, code)
            val request = HttpEntity(tokenRequest, headers)

            val response = restTemplate.postForObject(
                ACCESS_TOKEN_URL,
                request,
                OAuthAccessTokenResponse::class.java
            )

            if (response?.accessToken != null) {
                response.accessToken
            } else {
                if (response != null) {
                    log.warn("GitHub token exchange failed: error={}, description={}", response.error, response.errorDescription)
                } else {
                    log.warn("GitHub token exchange returned empty response")
                }
                null
            }
        } catch (e: RestClientException) {
            log.warn("GitHub token exchange request failed: {}", e.message)
            null
        }
    }

    private fun getUserInfo(accessToken: String): GitHubUserInfo? {
        return try {
            val headers = HttpHeaders().apply {
                setBearerAuth(accessToken)
                set("Accept", "application/vnd.github.v3+json")
            }
            val request = HttpEntity<Void>(headers)

            val response = restTemplate.exchange(
                MEMBER_INFO_URL,
                HttpMethod.GET,
                request,
                GitHubUserInfo::class.java
            )

            response.body
        } catch (e: RestClientException) {
            log.warn("GitHub user info request failed: {}", e.message)
            null
        }
    }

    private fun loginError(
        status: HttpStatus,
        code: String,
        message: String
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(code, message))
    }
}
