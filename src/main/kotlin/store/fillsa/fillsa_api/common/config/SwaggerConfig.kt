package store.fillsa.fillsa_api.common.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SwaggerConfig(
    @Value("\${swagger.server-url}")
    private val serverUrl: String
) {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("필사 API")
                    .description("필사 API")
                    .version("1.0.0")
            )
            .addServersItem(
                Server().apply {
                    url = serverUrl
                    description = "API Server"
                }
            )
    }
}