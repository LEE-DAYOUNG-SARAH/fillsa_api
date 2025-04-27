package com.fillsa.fillsa_api.domain.oauth.service.callback

import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.service.MemberService
import com.fillsa.fillsa_api.domain.oauth.client.login.GoogleOAuthLoginWebClient
import com.fillsa.fillsa_api.domain.oauth.service.token.useCase.OAuthTokenUseCase
import org.springframework.stereotype.Service

@Service
class GoogleOAuthCallbackService(
    memberService: MemberService,
    googleOAuthClient: GoogleOAuthLoginWebClient,
): OAuthCallbackService(memberService, googleOAuthClient) {
}