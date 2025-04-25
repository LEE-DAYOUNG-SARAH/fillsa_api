package com.fillsa.fillsa_api.domain.members.member.service

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthUserInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MemberServiceTest @Autowired constructor(
    val memberRepository: MemberRepository,
    val sut: MemberService
) {
    @Test
    fun `로그인 처리 성공 - 기존 회원`() {
        // given
        val existing = memberRepository.save(
            Member(
                oauthId = "oauth-123",
                oauthProvider = Member.OAuthProvider.KAKAO,
                nickname = "oldNick",
                profileImageUrl = "oldUrl"
            )
        )
        assertEquals(1, memberRepository.count())

        // when
        val info = OAuthUserInfo(
            id = "oauth-123",
            nickname = "ignoredNick",
            profileImageUrl = "ignoredUrl",
            oAuthProvider = Member.OAuthProvider.KAKAO
        )
        val result = sut.processOauthLogin(info)

        // then
        assertEquals(existing.memberSeq, result.memberSeq)
        assertEquals("oldNick", result.nickname)
        assertEquals("oldUrl", result.profileImageUrl)
        assertEquals(1, memberRepository.count())
    }

    @Test
    fun `로그인 처리 성공 - 신규 회원`() {
        // given
        assertEquals(0, memberRepository.count())

        // when
        val info = OAuthUserInfo(
            id = "new-oauth-456",
            nickname = "newNick",
            profileImageUrl = "newUrl",
            oAuthProvider = Member.OAuthProvider.GOOGLE
        )
        val result = sut.processOauthLogin(info)

        // then
        assertNotNull(result.memberSeq)
        assertEquals("new-oauth-456", result.oauthId)
        assertEquals(Member.OAuthProvider.GOOGLE, result.oauthProvider)
        assertEquals("newNick", result.nickname)
        assertEquals("newUrl", result.profileImageUrl)
        assertEquals(1, memberRepository.count())
    }
}