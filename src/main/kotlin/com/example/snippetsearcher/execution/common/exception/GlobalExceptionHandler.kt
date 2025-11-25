package com.example.snippetsearcher.execution.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException, request: HttpServletRequest): ResponseEntity<ApiError> {
        return ResponseEntity
            .status(e.status)
            .body(
                ApiError(
                    status = e.status.value(),
                    error = e.status.reasonPhrase,
                    message = e.message,
                    path = request.requestURI,
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(e: Exception, request: HttpServletRequest): ResponseEntity<ApiError> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiError(
                    status = 500,
                    error = "Internal Server Error",
                    message = e.message,
                    path = request.requestURI,
                ),
            )
    }
}
