package com.board.growtime.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class JwtServiceTest {

    private val jwtService = JwtService(
        configuredSecret = "test-secret-test-secret-test-secret",
        expirationSeconds = 3600
    )

    @Test
    fun `creates and parses token`() {
        val issuedAt = Instant.parse("2026-04-24T00:00:00Z")

        val token = jwtService.createToken("118417552", "ttaebari", issuedAt)
        val user = jwtService.parseToken(token, issuedAt.plusSeconds(60))

        assertThat(user.githubId).isEqualTo("118417552")
        assertThat(user.login).isEqualTo("ttaebari")
    }

    @Test
    fun `rejects expired token`() {
        val issuedAt = Instant.parse("2026-04-24T00:00:00Z")
        val token = jwtService.createToken("118417552", "ttaebari", issuedAt)

        assertThatThrownBy {
            jwtService.parseToken(token, issuedAt.plusSeconds(3601))
        }.isInstanceOf(JwtAuthenticationException::class.java)
    }

    @Test
    fun `rejects tampered token`() {
        val token = jwtService.createToken("118417552", "ttaebari")
        val tamperedToken = token.replaceAfterLast(".", "invalid-signature")

        assertThatThrownBy {
            jwtService.parseToken(tamperedToken)
        }.isInstanceOf(JwtAuthenticationException::class.java)
    }
}
