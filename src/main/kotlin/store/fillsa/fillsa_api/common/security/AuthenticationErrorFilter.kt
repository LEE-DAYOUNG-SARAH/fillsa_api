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
import java.time.LocalDateTime

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
            createErrorResponse(response, "토큰이 만료되었습니다. 토큰을 갱신해주세요.")
        } catch (e: JwtException) {
            log.warn { "유효하지 않은 토큰: ${e.message}" }
            createErrorResponse(response, "유효하지 않은 토큰입니다. 다시 로그인 해주세요.")
        }
    }

    private fun createErrorResponse(response: HttpServletResponse, message: String) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        val body = mapOf(
            "timestamp" to LocalDateTime.now().toString(),
            "status"    to response.status,
            "error"     to HttpStatus.UNAUTHORIZED.reasonPhrase,
            "message"   to message
        )

        response.writer.write(gson.toJson(body))
    }
}
