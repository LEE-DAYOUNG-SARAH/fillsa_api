package com.fillsa.admin.common.exception

import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = KotlinLogging.logger { }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        log.warn { "[BusinessException] code=${ex.errorCode.code}, message=${ex.message}" }

        val message = ex.message ?: ex.errorCode.message
        val response = ErrorResponse.from(ex.errorCode.httpStatus, ex.errorCode, message)
        return ResponseEntity(response, ex.errorCode.httpStatus)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        val errorMessage = errors.entries.joinToString("; ") { "${it.key}: ${it.value}" }
        log.warn { "[ValidationException] $errorMessage" }

        val response = ErrorResponse.from(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, errorMessage)
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.message ?: "예상치 못한 오류가 발생했습니다"
        log.error(ex) { "[UnhandledException] ${ex.javaClass.simpleName} → $errorMessage" }

        val response = ErrorResponse.from(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SERVER_ERROR, errorMessage)
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
