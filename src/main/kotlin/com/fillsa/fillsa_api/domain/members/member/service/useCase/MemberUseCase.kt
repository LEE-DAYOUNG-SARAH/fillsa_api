package com.fillsa.fillsa_api.domain.members.member.service.useCase

import com.fillsa.fillsa_api.domain.oauth.client.login.useCase.OAuthUserInfo
import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface MemberUseCase {
    /**
     *  회원가입 또는 로그인 처리
     */
    fun processOauthLogin(oAuthUserInfo: OAuthUserInfo): Member

    /**
     *  탈퇴
     */
    fun withdrawal(member: Member)
}