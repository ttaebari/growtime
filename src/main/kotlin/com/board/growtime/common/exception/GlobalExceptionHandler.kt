package com.board.growtime.common.exception

import com.board.growtime.common.response.ApiResponse
import com.board.growtime.common.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Business Exception: code={}, message={}", e.errorCode, e.message)
        
        val status = when (e.errorCode) {
            "USER_NOT_FOUND", "NOTE_NOT_FOUND" -> HttpStatus.NOT_FOUND
            "INVALID_USER_DATA", "INVALID_NOTE_DATA", 
            "INVALID_SERVICE_DATE", "SERVICE_DATE_NOT_SET", 
            "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(e.errorCode, e.message ?: "Unknown error"))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unexpected Exception", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."))
    }
}
