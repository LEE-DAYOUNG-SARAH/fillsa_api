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
import store.fillsa.fillsa_api.common.security.CustomUserDetailsService
import store.fillsa.fillsa_api.common.security.JwtAuthenticationFilter
import store.fillsa.fillsa_api.common.security.JwtTokenProvider
import store.fillsa.fillsa_api.common.security.PublicEndpoint
import store.fillsa.fillsa_api.domain.members.member.service.MemberService

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