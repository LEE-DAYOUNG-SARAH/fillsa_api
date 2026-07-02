package com.fillsa.admin.common.security

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fillsa.admin.common.exception.ErrorResponse
import com.fillsa.util.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 어드민 JWT 인증 필터.
 *  - Authorization: Bearer <token> 을 검증한다.
 *  - tokenType=ADMIN 이 아닌 토큰(=앱 회원 토큰 등)은 인증되지 않는다 → 앱 토큰의 어드민 접근 차단.
 *  - refresh 토큰(kind=REFRESH)으로는 API 인증 불가.
 */
class AdminJwtAuthenticationFilter(
    private val adminJwtTokenProvider: AdminJwtTokenProvider,
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger { }
    private val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val claims = adminJwtTokenProvider.parseClaims(token)

            if (!adminJwtTokenProvider.isAdminToken(claims) || adminJwtTokenProvider.isRefreshToken(claims)) {
                writeError(response, ErrorCode.ADMIN_TOKEN_INVALID)
                return
            }

            val principal = AdminPrincipal(
                adminSeq = adminJwtTokenProvider.getAdminSeq(claims),
                role = adminJwtTokenProvider.getRole(claims),
            )
            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(SimpleGrantedAuthority(principal.role.authority())),
            )
            SecurityContextHolder.getContext().authentication = authentication

            filterChain.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            log.warn { "만료된 어드민 토큰: ${e.message}" }
            writeError(response, ErrorCode.ADMIN_TOKEN_EXPIRED)
        } catch (e: JwtException) {
            log.warn { "유효하지 않은 어드민 토큰: ${e.message}" }
            writeError(response, ErrorCode.ADMIN_TOKEN_INVALID)
        } catch (e: Exception) {
            log.warn { "어드민 토큰 처리 오류: ${e.message}" }
            writeError(response, ErrorCode.ADMIN_TOKEN_INVALID)
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

    private fun writeError(response: HttpServletResponse, errorCode: ErrorCode) {
        SecurityContextHolder.clearContext()
        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        val body = ErrorResponse.from(errorCode.httpStatus, errorCode, errorCode.message)
        response.writer.write(objectMapper.writeValueAsString(body))
        response.writer.flush()
    }
}
