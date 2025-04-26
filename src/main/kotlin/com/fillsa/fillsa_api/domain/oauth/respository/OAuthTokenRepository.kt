package com.fillsa.fillsa_api.domain.oauth.respository

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.entity.OAuthToken
import org.springframework.data.jpa.repository.JpaRepository

interface OAuthTokenRepository: JpaRepository<OAuthToken, Long> {
    fun findTopByMemberOrderByOauthTokenSeqDesc(member: Member): OAuthToken?
}