package com.board.growtime.auth

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class AuthenticatedUser(
    val githubId: String,
    val login: String
)

class JwtAuthenticationException(message: String) : RuntimeException(message)

@Service
class JwtService(
    @Value("\${app.jwt.secret:growtime-local-development-secret-change-me}")
    configuredSecret: String,
    @Value("\${app.jwt.expiration-seconds:86400}")
    private val expirationSeconds: Long
) {
    private val objectMapper = ObjectMapper()
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()
    private val signingKey = configuredSecret
        .ifBlank { "growtime-local-development-secret-change-me" }
        .toByteArray(StandardCharsets.UTF_8)

    fun createToken(githubId: String, login: String, now: Instant = Instant.now()): String {
        val header = mapOf(
            "alg" to "HS256",
            "typ" to "JWT"
        )
        val payload = mapOf(
            "sub" to githubId,
            "login" to login,
            "iat" to now.epochSecond,
            "exp" to now.plusSeconds(expirationSeconds).epochSecond
        )

        val encodedHeader = encodeJson(header)
        val encodedPayload = encodeJson(payload)
        val unsignedToken = "$encodedHeader.$encodedPayload"
        val signature = encoder.encodeToString(sign(unsignedToken))

        return "$unsignedToken.$signature"
    }

    fun parseToken(token: String, now: Instant = Instant.now()): AuthenticatedUser {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw JwtAuthenticationException("Invalid token format")
        }

        val unsignedToken = "${parts[0]}.${parts[1]}"
        val expectedSignature = sign(unsignedToken)
        val actualSignature = decode(parts[2])
        if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
            throw JwtAuthenticationException("Invalid token signature")
        }

        val payload = parsePayload(parts[1])
        val expiresAt = (payload["exp"] as? Number)?.toLong()
            ?: throw JwtAuthenticationException("Missing token expiration")
        if (expiresAt <= now.epochSecond) {
            throw JwtAuthenticationException("Expired token")
        }

        val githubId = payload["sub"] as? String
            ?: throw JwtAuthenticationException("Missing token subject")
        val login = payload["login"] as? String ?: ""

        if (githubId.isBlank()) {
            throw JwtAuthenticationException("Missing token subject")
        }

        return AuthenticatedUser(githubId = githubId, login = login)
    }

    private fun encodeJson(value: Map<String, *>): String {
        val json = objectMapper.writeValueAsBytes(value)
        return encoder.encodeToString(json)
    }

    private fun parsePayload(encodedPayload: String): Map<String, Any> {
        return try {
            val json = String(decode(encodedPayload), StandardCharsets.UTF_8)
            objectMapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
        } catch (e: Exception) {
            throw JwtAuthenticationException("Invalid token payload")
        }
    }

    private fun sign(value: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(signingKey, "HmacSHA256"))
        return mac.doFinal(value.toByteArray(StandardCharsets.UTF_8))
    }

    private fun decode(value: String): ByteArray {
        return try {
            decoder.decode(value)
        } catch (e: IllegalArgumentException) {
            throw JwtAuthenticationException("Invalid token encoding")
        }
    }
}
