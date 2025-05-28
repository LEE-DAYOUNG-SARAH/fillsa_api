package store.fillsa.fillsa_api.domain.members.member.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.repository.MemberRepository

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MemberServiceTest @Autowired constructor(
    val memberRepository: MemberRepository,
    val sut: MemberService
) {
    @Test
    fun `회원가입 성공 - 기존 회원`() {
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
        val provider = Member.OAuthProvider.KAKAO
        val info = LoginRequest.UserData(
            id = "oauth-123",
            nickname = "ignoredNick",
            profileImageUrl = "ignoredUrl"
        )
        val result = sut.signUp(provider, info)

        // then
        assertEquals(existing.memberSeq, result.memberSeq)
        assertEquals("oldNick", result.nickname)
        assertEquals("oldUrl", result.profileImageUrl)
        assertEquals(1, memberRepository.count())
    }

    @Test
    fun `회원가입 성공 - 신규 회원`() {
        // given
        assertEquals(0, memberRepository.count())

        // when
        val provider = Member.OAuthProvider.GOOGLE
        val info = LoginRequest.UserData(
            id = "new-oauth-456",
            nickname = "newNick",
            profileImageUrl = "newUrl"
        )
        val result = sut.signUp(provider, info)

        // then
        assertNotNull(result.memberSeq)
        assertEquals("new-oauth-456", result.oauthId)
        assertEquals(Member.OAuthProvider.GOOGLE, result.oauthProvider)
        assertEquals("newNick", result.nickname)
        assertEquals("newUrl", result.profileImageUrl)
        assertEquals(1, memberRepository.count())
    }
}