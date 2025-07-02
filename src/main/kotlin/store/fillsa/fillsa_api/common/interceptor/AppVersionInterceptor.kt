package store.fillsa.fillsa_api.common.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import store.fillsa.fillsa_api.domain.appVersion.service.AppVersionService

@Component
class AppVersionInterceptor(
    private val appVersionService: AppVersionService
): HandlerInterceptor {
    private val log = KotlinLogging.logger {  }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val clientVersion = request.getHeader("X-App-Version")
        log.debug { "clientVersion = [$clientVersion]" }

        appVersionService.verifyAppVersion(clientVersion)

        return true
    }
}