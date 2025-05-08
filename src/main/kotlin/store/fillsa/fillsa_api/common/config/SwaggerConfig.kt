package store.fillsa.fillsa_api.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SwaggerConfig(
    @Value("\${swagger.server-url}")
    private val serverUrl: String,
    private val swaggerOperationCustomizer: SwaggerOperationCustomizer
) {

    private final val SECURITY_SCHEME_NAME = "bearerAuth "

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(createInfo())
            .addServersItem(createServerItem())
            .components(createComponents())
            .addSecurityItem(SecurityRequirement().addList(SECURITY_SCHEME_NAME))
    }

    private fun createInfo(): Info {
        return Info()
            .title("필사 API")
            .description("필사 API")
            .version("1.0.0")
    }

    private fun createServerItem(): Server {
        return Server().apply {
            url = serverUrl
            description = "API Server"
        }
    }

    private fun createComponents(): Components = Components()
        .addSecuritySchemes(
            SECURITY_SCHEME_NAME,
            SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer authentication")
        )

    @Bean
    fun operationCustomizer(): OperationCustomizer = swaggerOperationCustomizer
}