package store.fillsa.fillsa_api.common.security

import com.nimbusds.jose.shaded.gson.Gson
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.exception.ErrorCode.JWT_ACCESS_TOKEN_EXPIRED
import store.fillsa.fillsa_api.common.exception.ErrorCode.JWT_ACCESS_TOKEN_INVALID
import store.fillsa.fillsa_api.common.exception.ErrorResponse

@Component
class AuthenticationErrorFilter: HttpFilter() {
    private val log = KotlinLogging.logger {  }
    private val gson = Gson()

    override fun doFilter(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        try {
            chain.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            log.error { "만료된 토큰: ${e.message}" }
            createErrorResponse(response, JWT_ACCESS_TOKEN_EXPIRED)
        } catch (e: JwtException) {
            log.warn { "유효하지 않은 토큰: ${e.message}" }
            createErrorResponse(response, JWT_ACCESS_TOKEN_INVALID)
        }
    }

    private fun createErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        response.apply {
            status = HttpStatus.UNAUTHORIZED.value()
            contentType = MediaType.APPLICATION_JSON_VALUE
            characterEncoding = Charsets.UTF_8.name()
        }

        val body = ErrorResponse.from(HttpStatus.UNAUTHORIZED, errorCode, errorCode.message)

        response.writer.write(gson.toJson(body))
    }
}
