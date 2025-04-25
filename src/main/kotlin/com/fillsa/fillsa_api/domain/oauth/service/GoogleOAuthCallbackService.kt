package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.service.MemberService
import com.fillsa.fillsa_api.domain.oauth.client.GoogleOAuthLoginClient
import org.springframework.stereotype.Service

@Service
class GoogleOAuthCallbackService(
    memberService: MemberService,
    jwtTokenProvider: JwtTokenProvider,
    googleOAuthClient: GoogleOAuthLoginClient
): OAuthCallbackService(memberService, jwtTokenProvider, googleOAuthClient) {
    override var provider = googleOAuthClient.getOAuthProvider()
}