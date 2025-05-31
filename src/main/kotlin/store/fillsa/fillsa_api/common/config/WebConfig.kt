package store.fillsa.fillsa_api.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${fillsa.withdraw-url}")
    private val withdrawUrl: String
): WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(withdrawUrl)
            .allowedMethods("DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}