package store.fillsa.fillsa_api.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.service.MemberService

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberService: MemberService,
    private val publicEndpoint: PublicEndpoint,
) : OncePerRequestFilter() {
    val log = KotlinLogging.logger {  }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val memberSeq = jwtTokenProvider.getMemberSeqFromToken(token)
            log.info { "REQUEST memberSeq: $memberSeq" }

            val member = memberService.getActiveMemberBySeq(memberSeq)

            val authentication = createAuthentication(member)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    private fun createAuthentication(member: Member): Authentication {
        return UsernamePasswordAuthenticationToken(
            member,
            null,
            emptyList()
        )
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return publicEndpoint.getPublicPatterns().any { request.servletPath.startsWith(it) }
    }
}