package com.fillsa.fillsa_api.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    // Swagger 관련 엔드포인트는 인증 없이 접근 허용
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/**"   // 시큐리티 설정 전 모든 요청 허용
                    )
                    .permitAll()
                    // 그 외의 모든 요청은 인증 필요
                    .anyRequest()
                    .authenticated()
            }
            .httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults())

        return http.build()
    }
}