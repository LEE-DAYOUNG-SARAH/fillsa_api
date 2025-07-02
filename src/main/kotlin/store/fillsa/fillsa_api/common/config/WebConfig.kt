package store.fillsa.fillsa_api.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import store.fillsa.fillsa_api.common.interceptor.AppVersionInterceptor

@Configuration
class WebConfig(
    @Value("\${fillsa.withdraw-url}")
    private val withdrawUrl: String,
    private val appVersionInterceptor: AppVersionInterceptor
): WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(appVersionInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/v1/oauth/**"
            )
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(withdrawUrl)
            .allowedMethods("DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}