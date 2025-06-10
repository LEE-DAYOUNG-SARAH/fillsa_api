package store.fillsa.fillsa_api.common.security

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.exception.ErrorResponse
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.service.MemberService

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberService: MemberService,
    private val publicEndpoint: PublicEndpoint,
) : OncePerRequestFilter() {
    val log = KotlinLogging.logger {  }
    val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = resolveToken(request)

            if (token != null && jwtTokenProvider.validateToken(token)) {
                val memberSeq = jwtTokenProvider.getMemberSeqFromToken(token)
                log.info { "REQUEST memberSeq: $memberSeq" }

                val member = memberService.getActiveMemberBySeq(memberSeq)

                val authentication = createAuthentication(member)
                SecurityContextHolder.getContext().authentication = authentication
            }

            filterChain.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            log.error { "만료된 토큰: ${e.message}" }
            handleJwtException(response, ErrorCode.JWT_ACCESS_TOKEN_EXPIRED)
        } catch (e: JwtException) {
            log.warn { "유효하지 않은 토큰: ${e.message}" }
            handleJwtException(response, ErrorCode.JWT_ACCESS_TOKEN_INVALID)
        } catch (e: BusinessException) {
            log.warn { e.message }
            handleJwtException(response, e.errorCode)
        } catch (e: Exception) {
            log.warn { e.message }
            handleJwtException(response, ErrorCode.UNEXPECTED_EXCEPTION)
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    private fun createAuthentication(member: Member): Authentication {
        return UsernamePasswordAuthenticationToken(
            member,
            null,
            emptyList()
        )
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return publicEndpoint.getPublicPatterns().any { pattern ->
            AntPathMatcher().match(pattern, path)
        }
    }

    private fun handleJwtException(response: HttpServletResponse, errorCode: ErrorCode) {
        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        val body = ErrorResponse.from(errorCode.httpStatus, errorCode, errorCode.message)
        response.writer.write(objectMapper.writeValueAsString(body))
        response.writer.flush()
    }
}