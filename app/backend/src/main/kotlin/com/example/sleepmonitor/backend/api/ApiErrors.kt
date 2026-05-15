package com.example.sleepmonitor.backend.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiError(
    val status: Int,
    val error: String,
    val message: String
)

class BadRequestException(message: String) : RuntimeException(message)

class NotFoundException(message: String) : RuntimeException(message)

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException::class)
    fun badRequest(error: BadRequestException): ResponseEntity<ApiError> =
        apiError(HttpStatus.BAD_REQUEST, error.message.orEmpty())

    @ExceptionHandler(NotFoundException::class)
    fun notFound(error: NotFoundException): ResponseEntity<ApiError> =
        apiError(HttpStatus.NOT_FOUND, error.message.orEmpty())

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidArgument(error: IllegalArgumentException): ResponseEntity<ApiError> =
        apiError(HttpStatus.BAD_REQUEST, error.message.orEmpty())

    private fun apiError(status: HttpStatus, message: String): ResponseEntity<ApiError> =
        ResponseEntity.status(status).body(
            ApiError(
                status = status.value(),
                error = status.reasonPhrase,
                message = message.ifBlank { "Solicitud no valida" }
            )
        )
}
