package com.fillsa.fillsa_api.domain.auth.service

import com.fillsa.fillsa_api.common.exception.InvalidRequestException
import com.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.auth.service.useCase.AuthUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class KakaoAuthService(
    private val memberRepository: MemberRepository,
    private val jwtTokenProvider: JwtTokenProvider
): AuthUseCase {
    override fun login(request: LoginRequest): LoginResponse {
        // 1. 카카오 액세스 토큰 받기
        val accessToken = getAccessToken(request.code)

        // 2. 카카오 사용자 정보 받기
        val authMember = getAuthMember(accessToken)

        // 3. 회원가입 또는 로그인 처리
        val member = memberRepository.findByMemberSeqAndOauthProvider(
            memberSeq = authMember.memberSeq,
            oauthProvider = Member.OauthProvider.KAKAO
        ) ?: memberRepository.save(
            Member.createOAuthMember(
                oauthId = authMember.memberSeq.toString(),
                oauthProvider = Member.OauthProvider.KAKAO,
                email = authMember.email.orEmpty(),
                nickname = authMember.nickname.orEmpty(),
                profileImageUrl = authMember.profileImageUrl
            )
        )

        // 4. JWT 토큰 생성
        val tokenInfo = jwtTokenProvider.createTokens(member.memberSeq)

        // 5. 응답 반환
        return LoginResponse(
            accessToken = tokenInfo.accessToken,
            refreshToken = tokenInfo.refreshToken,
            memberSeq = member.memberSeq,
            email = member.email.orEmpty(),
            nickname = member.nickname.orEmpty()
        )
    }

    // 토큰 갱신
    override fun refreshToken(refreshToken: String): TokenInfo {
        // 1. 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw InvalidRequestException("유효하지 않은 리프레시 토큰입니다.")
        }

        // 2. 토큰에서 사용자 ID 추출
        val memberId = jwtTokenProvider.getMemberSeqFromToken(refreshToken)

        // 3. 새로운 토큰 발급
        return jwtTokenProvider.createTokens(memberId)
    }

    override fun getAccessToken(code: String): String {
        TODO("Not yet implemented")
    }

    override fun getAuthMember(accessToken: String): Member {
        TODO("Not yet implemented")
    }
}