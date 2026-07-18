package com.fillsa.admin.config

import com.fillsa.admin.common.security.AdminAuthenticationEntryPoint
import com.fillsa.admin.common.security.AdminJwtAuthenticationFilter
import com.fillsa.admin.common.security.AdminJwtTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * 어드민 BFF 보안 설정. 앱(bff:app)의 OAuth 인증과 독립적으로 동작합니다.
 * 인증 주체는 service:admin 의 admins 테이블입니다.
 *
 * 토큰 분리: bff:app 과 다른 서명 키(admin-jwt.secret) + tokenType=ADMIN 클레임을 사용하므로
 * 앱 회원 토큰으로는 어드민 엔드포인트에 인증할 수 없습니다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class AdminSecurityConfig(
    private val adminJwtTokenProvider: AdminJwtTokenProvider,
    private val adminAuthenticationEntryPoint: AdminAuthenticationEntryPoint,
    // 어드민 콘솔 오리진. 콤마로 다중 지정 가능 (local 기본: Vite 개발 서버 / prod: admin-cors.allowed-origins 로 override)
    @Value("\${admin-cors.allowed-origins:http://localhost:5173}")
    private val allowedOriginsValue: String,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { req ->
                req
                    .requestMatchers(
                        "/ping",
                        "/api/admin/v1/auth/login",
                        "/api/admin/v1/auth/refresh",
                        "/actuator/health",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint(adminAuthenticationEntryPoint) }
            .addFilterBefore(
                AdminJwtAuthenticationFilter(adminJwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java,
            )
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = allowedOriginsValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
