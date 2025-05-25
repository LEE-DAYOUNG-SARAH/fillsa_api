package store.fillsa.fillsa_api.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter
import store.fillsa.fillsa_api.common.logging.RequestLoggingFilter
import store.fillsa.fillsa_api.common.security.AuthenticationErrorFilter
import store.fillsa.fillsa_api.common.security.CustomUserDetailsService
import store.fillsa.fillsa_api.common.security.JwtAuthenticationFilter
import store.fillsa.fillsa_api.common.security.PublicEndpoint

@Configuration
class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationErrorFilter: AuthenticationErrorFilter,
    private val requestLoggingFilter: RequestLoggingFilter,
    private val publicEndpoint: PublicEndpoint,
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
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(authenticationErrorFilter, JwtAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }
}