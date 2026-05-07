package com.board.growtime.config

import com.board.growtime.auth.ApiAuthenticationInterceptor
import com.board.growtime.auth.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class CorsConfigTest {

    private val interceptor = ApiAuthenticationInterceptor(
        jwtService = JwtService(
            configuredSecret = "test-secret-test-secret-test-secret",
            expirationSeconds = 3600
        ),
        objectMapper = ObjectMapper()
    )

    @Test
    fun `cors configuration allows only exact configured origins`() {
        val source = CorsConfig(
            allowedOrigins = "https://growtime-frontend-nine.vercel.app",
            apiAuthenticationInterceptor = interceptor
        ).corsConfigurationSource()
        val request = MockHttpServletRequest("GET", "/api/user/118417552")
        val configuration = source.getCorsConfiguration(request)!!

        assertThat(configuration.allowedOrigins).containsExactly(
            "https://growtime-frontend-nine.vercel.app"
        )
        assertThat(configuration.allowedOriginPatterns).isNullOrEmpty()
        assertThat(configuration.allowedMethods).containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS")
        assertThat(configuration.allowedHeaders).containsExactly("Authorization", "Content-Type", "Accept")
        assertThat(configuration.checkOrigin("https://growtime-frontend-nine.vercel.app"))
            .isEqualTo("https://growtime-frontend-nine.vercel.app")
        assertThat(configuration.checkOrigin("https://preview-growtime.vercel.app")).isNull()
    }

    @Test
    fun `cors configuration rejects wildcard origins`() {
        assertThatThrownBy {
            CorsConfig(
                allowedOrigins = "https://growtime-frontend-nine.vercel.app,https://*.vercel.app",
                apiAuthenticationInterceptor = interceptor
            ).corsConfigurationSource()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Wildcard patterns are not allowed")
    }
}
