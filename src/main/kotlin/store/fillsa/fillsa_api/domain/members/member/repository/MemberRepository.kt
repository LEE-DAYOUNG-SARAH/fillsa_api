package store.fillsa.fillsa_api.domain.members.member.repository

import store.fillsa.fillsa_api.domain.members.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository: JpaRepository<Member, Long> {
    fun findByOauthIdAndOauthProvider(oauthId: String, oauthProvider: Member.OAuthProvider): Member?
}