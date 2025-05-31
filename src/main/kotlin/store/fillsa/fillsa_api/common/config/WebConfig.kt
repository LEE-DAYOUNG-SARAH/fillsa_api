package store.fillsa.fillsa_api.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("https://delete.fillsa.store", "http://localhost:3000") // 프론트 주소
            .allowedMethods("DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}