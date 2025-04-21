package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.oauth.client.KakaoOAuthClient
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.MemberService
import org.springframework.stereotype.Service

@Service
class KakaoOAuthService(
    memberService: MemberService,
    jwtTokenProvider: JwtTokenProvider,
    kakaoOAuthClient: KakaoOAuthClient
): OAuthService(memberService, jwtTokenProvider, kakaoOAuthClient) {
    override var provider = Member.OAuthProvider.KAKAO
}