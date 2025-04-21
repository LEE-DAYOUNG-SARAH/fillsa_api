package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.oauth.client.GoogleOAuthClient
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.MemberService
import org.springframework.stereotype.Service

@Service
class GoogleOAuthService(
    memberService: MemberService,
    jwtTokenProvider: JwtTokenProvider,
    googleOAuthClient: GoogleOAuthClient
): OAuthService(memberService, jwtTokenProvider, googleOAuthClient) {
    override var provider = Member.OAuthProvider.GOOGLE
}