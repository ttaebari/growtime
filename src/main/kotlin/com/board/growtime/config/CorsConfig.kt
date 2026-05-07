package com.board.growtime.config

import com.board.growtime.auth.ApiAuthenticationInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    @Value("\${app.cors.allowed-origins}")
    private val allowedOrigins: String,
    private val apiAuthenticationInterceptor: ApiAuthenticationInterceptor
) : WebMvcConfigurer {

    private val corsAllowedMethods = arrayOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
    private val corsAllowedHeaders = arrayOf("Authorization", "Content-Type", "Accept")

    private fun configuredOrigins(): Array<String> {
        val origins = allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        require(origins.none { it.contains("*") }) {
            "CORS allowed origins must be exact origins. Wildcard patterns are not allowed."
        }

        return origins.toTypedArray()
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(*configuredOrigins())
            .allowedMethods(*corsAllowedMethods)
            .allowedHeaders(*corsAllowedHeaders)
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiAuthenticationInterceptor)
            .addPathPatterns("/api/**")
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            configuredOrigins().forEach { addAllowedOrigin(it) }
            corsAllowedMethods.forEach { addAllowedMethod(it) }
            corsAllowedHeaders.forEach { addAllowedHeader(it) }
            allowCredentials = true
        }
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
} 
