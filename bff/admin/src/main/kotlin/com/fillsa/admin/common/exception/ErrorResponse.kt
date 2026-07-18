package com.fillsa.admin.common.exception

import com.fillsa.util.exception.ErrorCode
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

/**
 * fillsa-api 공통 에러 형식. bff:app 의 ErrorResponse 와 동일한 필드 구조.
 */
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val httpStatus: Int,
    val errorCode: Int,
    val message: String,
) {
    companion object {
        fun from(httpStatus: HttpStatus, errorCode: ErrorCode, message: String) = ErrorResponse(
            timestamp = LocalDateTime.now(),
            httpStatus = httpStatus.value(),
            errorCode = errorCode.code,
            message = message,
        )
    }
}
