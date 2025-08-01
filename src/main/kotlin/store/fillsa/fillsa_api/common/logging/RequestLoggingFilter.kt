package store.fillsa.fillsa_api.common.logging

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.nio.charset.StandardCharsets

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        if (isMultipartRequest(request)) {
            chain.doFilter(request, response)
            return
        }

        val requestWrapper = try {
            CustomHttpServletRequestWrapper(request).also { wrapper ->
                val method = request.method
                val url = getRequestUrl(request)

                val logMsg = if (method =="GET") {
                    "📥 $method $url"
                } else {
                    val raw = wrapper.getCachedBody()
                    val body = parseBody(raw)

                    "📥 $method $url | body=$body"
                }

                log.info { logMsg }
            }
        } catch (e: Exception) {
            log.error(e) { "RequestLoggingFilter error, proceeding with original request" }
            request
        }

        chain.doFilter(requestWrapper, response)
    }

    private fun isMultipartRequest(request: HttpServletRequest): Boolean =
        request.contentType
            ?.contains("multipart/", ignoreCase = true)
            ?: false

    private fun getRequestUrl(request: HttpServletRequest): String {
        val uri = request.requestURI
        val qs  = request.queryString
        return if (qs.isNullOrEmpty()) uri else "$uri?$qs"
    }

    private fun parseBody(rawBytes: ByteArray): String {
        return try {
            val jsonNode = mapper.readTree(rawBytes)
            mapper.writeValueAsString(jsonNode)
        } catch (ex: Exception) {
            String(rawBytes, StandardCharsets.UTF_8)
        }
    }
}
