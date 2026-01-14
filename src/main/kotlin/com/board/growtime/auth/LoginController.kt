package com.board.growtime.core

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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
class LoginController(
    private val restTemplate: RestTemplate,
    private val gitHubUserService: GitHubUserService
) {
    private val log = LoggerFactory.getLogger(LoginController::class.java)

    @Value("\${github.clientId}")
    private lateinit var clientId: String

    @Value("\${github.clientSecret}")
    private lateinit var clientSecret: String

    companion object {
        private const val ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token"
        private const val MEMBER_INFO_URL = "https://api.github.com/user"
        private const val GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize"
    }

    @GetMapping("/login")
    fun login(): ApiResponse<Map<String, String>> {
        val authUrl = "$GITHUB_AUTH_URL?client_id=$clientId&scope=read:user,user:email"

        val response = mapOf(
            "authUrl" to authUrl,
            "message" to "GitHub 로그인을 위해 위 URL로 리다이렉트하세요"
        )

        return ApiResponse.success(response)
    }

    @GetMapping("/callback")
    fun handleCallback(
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) error: String?
    ): ApiResponse<LoginResponse> {
        log.info("GitHub 콜백 호출됨 - code: {}, error: {}", code, error)

        // OAuth 에러 처리
        if (error != null) {
            log.error("GitHub OAuth 에러: {}", error)
            return ApiResponse.error("OAUTH_ERROR", error)
        }

        if (code.isNullOrBlank()) {
            log.error("인증 코드가 없습니다")
            return ApiResponse.error("NO_AUTH_CODE", "인증 코드가 없습니다")
        }

        try {
            log.info("액세스 토큰 요청 시작")
            val accessToken = getAccessToken(code)
                ?: return ApiResponse.error("NO_TOKEN", "액세스 토큰을 받지 못했습니다")
            
            log.info("액세스 토큰 획득 성공")

            log.info("사용자 정보 요청 시작")
            val userInfo = getUserInfo(accessToken)
                ?: return ApiResponse.error("NO_USER_INFO", "사용자 정보를 받지 못했습니다")
            
            log.info("사용자 정보 획득 성공: {}", userInfo.login)

            // 사용자 정보를 데이터베이스에 저장 또는 업데이트
            log.info("사용자 정보 저장 시작")
            val savedUser = gitHubUserService.saveOrUpdateUser(
                userInfo.id.toString(),
                userInfo.login,
                userInfo.name,
                userInfo.avatarUrl,
                userInfo.htmlUrl,
                userInfo.location,
                accessToken
            )
            log.info("사용자 정보 저장 완료: {}", savedUser.githubId)

            val response = LoginResponse(
                accessToken = accessToken,
                refreshToken = null,
                githubId = savedUser.githubId
            )
            return ApiResponse.success(response)

        } catch (e: Exception) {
            log.error("콜백 처리 중 예외 발생", e)
            throw e // Let GlobalExceptionHandler handle it
        }
    }

    private fun getAccessToken(code: String): String? {
        return try {
            log.info("액세스 토큰 요청 - clientId: {}, code: {}", clientId, code)

            val headers = HttpHeaders().apply {
                set("Accept", "application/json")
            }

            val tokenRequest = OAuthAccessTokenRequest(clientId, clientSecret, code)
            val request = HttpEntity(tokenRequest, headers)

            log.info("GitHub API 호출: {}", ACCESS_TOKEN_URL)
            val response = restTemplate.postForObject(
                ACCESS_TOKEN_URL,
                request,
                OAuthAccessTokenResponse::class.java
            )

            if (response?.accessToken != null) {
                log.info("액세스 토큰 획득 성공")
                response.accessToken
            } else {
                if (response != null) {
                    log.error("액세스 토큰 응답 에러: {}, {}", response.error, response.errorDescription)
                } else {
                    log.error("액세스 토큰 응답이 null입니다")
                }
                null
            }
        } catch (e: Exception) {
            log.error("액세스 토큰 획득 중 에러 발생", e)
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
        } catch (e: Exception) {
            log.error("사용자 정보 획득 중 에러 발생", e)
            null
        }
    }
}