package store.fillsa.fillsa_api.domain.auth.security

import store.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberRepository: MemberRepository
): UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val memberSeq = username.toLong()
        return memberRepository.findById(memberSeq)
            .orElseThrow { UsernameNotFoundException("Member not found with seq: $memberSeq") }
    }
}