package store.fillsa.fillsa_api.common.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicEndpoint(
    @Value("\${fillsa.security.actuator-path}")
    private val actuatorPath: String,
    @Value("\${fillsa.security.swagger-path}")
    private val swaggerPathPrefix: String,
    @Value("\${springdoc.swagger-ui.path}")
    private val swaggerUiPath: String,
    @Value("\${springdoc.swagger-ui.url}")
    private val swaggerDocPath: String,
) {
    fun getPublicPatterns(): Array<String> {
        return arrayOf(
            // 내부 API 경로
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/oauth/**",
            "/api/v1/quotes/**",
            "/api/v1/notices",
            "/api/v1/app-versions",
            "/test/**",
            "/api/admin/**",
            "/api/v1/popups/**",

            // Actuator 경로
            actuatorPath,

            // Swagger 경로
            swaggerUiPath,
            swaggerDocPath,
            "$swaggerDocPath/**",
            "$swaggerPathPrefix/swagger-ui/**",

            // admob
            "/app-ads.txt"
        )
    }
}

