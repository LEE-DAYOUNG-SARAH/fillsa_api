package com.fillsa.service.member

import com.fillsa.service.member.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository: JpaRepository<Member, Long> {
    fun findAllByOauthIdAndOauthProvider(oauthId: String, oauthProvider: Member.OAuthProvider): List<Member>
}