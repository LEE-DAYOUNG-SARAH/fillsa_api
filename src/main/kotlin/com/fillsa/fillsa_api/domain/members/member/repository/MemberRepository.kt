package com.fillsa.fillsa_api.domain.members.member.repository

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository: JpaRepository<Member, Long> {
    fun findByMemberSeqAndOauthProvider(memberSeq: Long, oauthProvider: Member.OauthProvider): Member?
}