package com.board.growtime

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.web.client.RestTemplate
import java.time.Duration

@SpringBootApplication
@EnableJpaAuditing
class GrowtimeApplication {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .connectTimeout(Duration.ofSeconds(3))
            .readTimeout(Duration.ofSeconds(5))
            .build()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(GrowtimeApplication::class.java, *args)
} 
