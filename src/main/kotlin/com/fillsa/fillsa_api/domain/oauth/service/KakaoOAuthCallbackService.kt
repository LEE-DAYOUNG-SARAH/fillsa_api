package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.MemberService
import com.fillsa.fillsa_api.domain.oauth.client.KakaoOAuthLoginClient
import org.springframework.stereotype.Service

@Service
class KakaoOAuthCallbackService(
    memberService: MemberService,
    jwtTokenProvider: JwtTokenProvider,
    kakaoOAuthClient: KakaoOAuthLoginClient
): OAuthCallbackService(memberService, jwtTokenProvider, kakaoOAuthClient) {
    override var provider = Member.OAuthProvider.KAKAO
}