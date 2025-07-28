package store.fillsa.fillsa_api.common.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val SECURITY_SCHEME_NAME = "bearerAuth"

@Configuration
class SwaggerConfig(
    @Value("\${swagger.server-url}")
    private val serverUrl: String,
    private val swaggerOperationCustomizer: SwaggerOperationCustomizer
) {
    @Bean
    fun apiV1(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/api/v1/**")
            .addOperationCustomizer(swaggerOperationCustomizer)
            .build()
    }

    @Bean
    fun apiV2(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("v2")
            .pathsToMatch("/api/v2/**")
            .addOperationCustomizer(swaggerOperationCustomizer)
            .build()
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(createInfo())
            .addServersItem(createServerItem())
            .components(createComponents())
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