package com.fillsa.admin.common.security

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fillsa.admin.common.exception.ErrorResponse
import com.fillsa.util.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * 미인증 상태로 보호 엔드포인트 접근 시 공통 에러 형식(401)을 반환한다.
 */
@Component
class AdminAuthenticationEntryPoint : AuthenticationEntryPoint {

    private val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val errorCode = ErrorCode.ADMIN_TOKEN_INVALID
        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        val body = ErrorResponse.from(errorCode.httpStatus, errorCode, errorCode.message)
        response.writer.write(objectMapper.writeValueAsString(body))
        response.writer.flush()
    }
}
