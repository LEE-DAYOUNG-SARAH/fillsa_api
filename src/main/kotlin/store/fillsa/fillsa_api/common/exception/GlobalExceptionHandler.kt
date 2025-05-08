package store.fillsa.fillsa_api.common.exception

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler {
    val log = KotlinLogging.logger {  }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        log.error { ex.message }

        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            httpStatus = ex.errorCode.httpStatus.value(),
            error = ex.errorCode.httpStatus.reasonPhrase,
            errorCode = ex.errorCode.code,
            message = ex.errorCode.message
        )
        return ResponseEntity(response, ex.errorCode.httpStatus)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        val errorMessage = errors.entries.joinToString("; ") { "${it.key}: ${it.value}" }
        log.error { ex.message }

        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            httpStatus = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            errorCode = ErrorCode.INVALID_REQUEST.code,
            message = errorMessage
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error { ex.message }

        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            errorCode = ErrorCode.SERVER_ERROR.code,
            message = ex.message ?: "예상치 못한 오류가 발생했습니다"
        )
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val httpStatus: Int,
    val error: String,
    val errorCode: Int,
    val message: String
)
