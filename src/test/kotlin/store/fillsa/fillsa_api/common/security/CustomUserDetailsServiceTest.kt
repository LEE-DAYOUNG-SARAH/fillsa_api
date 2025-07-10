package store.fillsa.fillsa_api.common.security

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.UsernameNotFoundException
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.fixture.member.entity.MemberEntityFactory
import store.fillsa.fillsa_api.fixture.member.persist.MemberPersistFactory
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomUserDetailsServiceTest @Autowired constructor(
    private val memberPersistFactory: MemberPersistFactory,
    private val sut: CustomUserDetailsService
) {
    
    @Test
    fun `사용자 조회 성공 - 존재하는 회원 시퀀스로 조회한다`() {
        // given
        val member = memberPersistFactory.createMember(
            MemberEntityFactory.member(
                nickname = "testUser",
                oauthId = "oauth-123",
                oAuthProvider = Member.OAuthProvider.GOOGLE
            )
        )
        val username = member.memberSeq.toString()
        
        // when
        val result = sut.loadUserByUsername(username)
        
        // then
        assertThat(result).isEqualTo(member)
        assertThat(result.username).isEqualTo(member.memberSeq.toString())
    }
    
    @Test
    fun `사용자 조회 실패 - 존재하지 않는 회원 시퀀스로 조회하면 예외를 던진다`() {
        // given
        val memberSeq = 999L
        val username = "999"
        
        // when & then
        val exception = assertThrows<UsernameNotFoundException> {
            sut.loadUserByUsername(username)
        }
        assertThat(exception.message).isEqualTo("Member not found with seq: $memberSeq")
    }
    
    @Test
    fun `사용자 조회 실패 - 숫자가 아닌 username으로 조회하면 예외를 던진다`() {
        // given
        val username = "not-a-number"
        
        // when & then
        assertThrows<NumberFormatException> {
            sut.loadUserByUsername(username)
        }
    }
} 