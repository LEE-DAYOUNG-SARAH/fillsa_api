package com.fillsa.app.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter
import com.fillsa.app.common.logging.RequestLoggingFilter
import com.fillsa.app.common.security.CustomUserDetailsService
import com.fillsa.app.common.security.JwtAuthenticationFilter
import com.fillsa.app.common.security.JwtTokenProvider
import com.fillsa.app.common.security.PublicEndpoint
import com.fillsa.app.service.members.member.MemberService

@Configuration
class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService,
    private val requestLoggingFilter: RequestLoggingFilter,
    private val publicEndpoint: PublicEndpoint,
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberService: MemberService,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(*publicEndpoint.getPublicPatterns()).permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .userDetailsService(customUserDetailsService)
            .addFilterBefore(requestLoggingFilter, WebAsyncManagerIntegrationFilter::class.java)
            .addFilterBefore(JwtAuthenticationFilter(jwtTokenProvider, memberService, publicEndpoint), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }
}