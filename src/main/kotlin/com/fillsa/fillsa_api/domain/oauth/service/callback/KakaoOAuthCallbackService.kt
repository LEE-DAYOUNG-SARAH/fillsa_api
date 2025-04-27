package com.fillsa.fillsa_api.domain.oauth.service.callback

import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.service.MemberService
import com.fillsa.fillsa_api.domain.oauth.client.login.KakaoOAuthLoginWebClient
import com.fillsa.fillsa_api.domain.oauth.service.token.useCase.OAuthTokenUseCase
import org.springframework.stereotype.Service

@Service
class KakaoOAuthCallbackService(
    memberService: MemberService,
    kakaoOAuthClient: KakaoOAuthLoginWebClient
): OAuthCallbackService(memberService, kakaoOAuthClient) {
}