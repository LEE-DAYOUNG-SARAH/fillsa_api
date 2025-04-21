package com.fillsa.fillsa_api.domain.members.member.service.useCase

import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface MemberUseCase {
    /**
     *  회원가입 또는 로그인 처리
     */
    fun processOauthLogin(
        oauthId: String,
        nickname: String,
        profileImageUrl: String?,
        oauthProvider: Member.OauthProvider
    ): Member
}