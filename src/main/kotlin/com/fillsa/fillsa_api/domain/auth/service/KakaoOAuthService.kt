package com.fillsa.fillsa_api.domain.auth.service

import com.fillsa.fillsa_api.domain.auth.client.KakaoTokenClient
import com.fillsa.fillsa_api.domain.auth.client.KakaoUserClient
import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.auth.service.useCase.OauthUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import org.springframework.stereotype.Service

@Service
class KakaoOAuthService(
    private val kakaoTokenClient: KakaoTokenClient,
    private val kakaoUserClient: KakaoUserClient,
    private val memberUseCase: MemberUseCase,
    private val jwtTokenProvider: JwtTokenProvider,
): OauthUseCase {

    override fun processOauthCallback(code: String): LoginResponse {
        // 1. 카카오 액세스 토큰 받기
        val accessToken = kakaoTokenClient.getAccessToken(code)

        // 2. 카카오 사용자 정보 받기
        val kakaoUser = kakaoUserClient.getUserInfo(accessToken)

        // 3. 회원가입 또는 로그인 처리
        val member = memberUseCase.processOauthLogin(
            oauthId = kakaoUser.id.toString(),
            nickname = kakaoUser.getNickname(),
            profileImageUrl = kakaoUser.getProfileImage(),
            oauthProvider = Member.OauthProvider.KAKAO
        )

        // 4. JWT 토큰 생성
        val tokenInfo = jwtTokenProvider.createTokens(member.memberSeq)

        // 5. 응답 반환
        return LoginResponse(
            accessToken = tokenInfo.accessToken,
            refreshToken = tokenInfo.refreshToken,
            memberSeq = member.memberSeq,
            nickname = member.nickname.orEmpty()
        )
    }
}