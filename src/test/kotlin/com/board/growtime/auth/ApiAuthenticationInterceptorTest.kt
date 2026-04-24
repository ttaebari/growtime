package com.board.growtime.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class ApiAuthenticationInterceptorTest {

    private val jwtService = JwtService(
        configuredSecret = "test-secret-test-secret-test-secret",
        expirationSeconds = 3600
    )
    private val interceptor = ApiAuthenticationInterceptor(jwtService, ObjectMapper())

    @Test
    fun `returns 401 when authorization header is missing`() {
        val request = MockHttpServletRequest("GET", "/api/user/118417552")
        val response = MockHttpServletResponse()

        val allowed = interceptor.preHandle(request, response, Any())

        assertThat(allowed).isFalse()
        assertThat(response.status).isEqualTo(401)
        assertThat(response.contentAsString).contains("UNAUTHORIZED")
    }

    @Test
    fun `returns 401 when token is invalid`() {
        val request = MockHttpServletRequest("GET", "/api/user/118417552")
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
        val response = MockHttpServletResponse()

        val allowed = interceptor.preHandle(request, response, Any())

        assertThat(allowed).isFalse()
        assertThat(response.status).isEqualTo(401)
        assertThat(response.contentAsString).contains("UNAUTHORIZED")
    }

    @Test
    fun `returns 403 when token githubId differs from path githubId`() {
        val request = MockHttpServletRequest("GET", "/api/user/999")
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${tokenFor("118417552")}")
        val response = MockHttpServletResponse()

        val allowed = interceptor.preHandle(request, response, Any())

        assertThat(allowed).isFalse()
        assertThat(response.status).isEqualTo(403)
        assertThat(response.contentAsString).contains("FORBIDDEN")
    }

    @Test
    fun `allows request when token githubId matches path githubId`() {
        val request = MockHttpServletRequest("GET", "/api/user/118417552")
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${tokenFor("118417552")}")
        val response = MockHttpServletResponse()

        val allowed = interceptor.preHandle(request, response, Any())
        val authenticatedGithubId = request.getAttribute(
            ApiAuthenticationInterceptor.AUTHENTICATED_GITHUB_ID_ATTRIBUTE
        ) as String?

        assertThat(allowed).isTrue()
        assertThat(response.status).isEqualTo(200)
        assertThat(authenticatedGithubId).isEqualTo("118417552")
    }

    @Test
    fun `applies same ownership rule to note service date and quick link paths`() {
        val protectedPaths = listOf(
            "/api/notes/999",
            "/api/user/999/service-dates",
            "/api/user/999/quick-links"
        )

        protectedPaths.forEach { path ->
            val request = MockHttpServletRequest("GET", path)
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${tokenFor("118417552")}")
            val response = MockHttpServletResponse()

            val allowed = interceptor.preHandle(request, response, Any())

            assertThat(allowed).isFalse()
            assertThat(response.status).isEqualTo(403)
        }
    }

    @Test
    fun `allows options preflight without token`() {
        val request = MockHttpServletRequest("OPTIONS", "/api/user/118417552")
        val response = MockHttpServletResponse()

        val allowed = interceptor.preHandle(request, response, Any())

        assertThat(allowed).isTrue()
    }

    private fun tokenFor(githubId: String): String {
        return jwtService.createToken(githubId, "ttaebari")
    }
}
