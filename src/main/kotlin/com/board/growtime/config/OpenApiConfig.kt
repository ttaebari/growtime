package com.board.growtime.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Growtime API")
                    .description("Growtime backend API documentation")
                    .version("v1")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )

    @Bean
    fun apiSecurityCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            openApi.paths
                ?.filterKeys { it.startsWith("/api/") || it == "/api" }
                ?.values
                ?.flatMap { it.readOperations() }
                ?.forEach { operation ->
                    operation.addSecurityItem(SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                }
        }

    companion object {
        private const val BEARER_AUTH_SCHEME = "bearerAuth"
    }
}
