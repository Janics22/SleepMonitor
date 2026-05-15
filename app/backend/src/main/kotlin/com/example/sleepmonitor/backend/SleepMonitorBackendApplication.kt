package com.example.sleepmonitor.backend

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class SleepMonitorBackendApplication

fun main(args: Array<String>) {
    runApplication<SleepMonitorBackendApplication>(*args)
}

@Configuration
class OpenApiConfiguration {
    @Bean
    fun sleepMonitorOpenApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("Sleep Monitor API")
            .version("1.0.0")
            .description("Backend REST para sincronizar usuarios y sesiones de sueno de la app Android.")
    )
}
