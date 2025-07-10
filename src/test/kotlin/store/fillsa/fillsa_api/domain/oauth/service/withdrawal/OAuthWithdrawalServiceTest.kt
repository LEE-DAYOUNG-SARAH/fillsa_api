package store.fillsa.fillsa_api.domain.oauth.service.withdrawal

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.domain.members.member.entity.Member

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OAuthWithdrawalServiceTest @Autowired constructor(
    private val sut: OAuthWithdrawalService
) {
    
    @Test
    fun `OAuth 탈퇴 실패 - 지원하지 않는 OAuth 제공자인 경우 예외를 던진다`() {
        // given
        val provider = Member.OAuthProvider.GOOGLE
        val code = "auth-code"
        
        // when & then
        assertThatThrownBy { sut.withdraw(provider, code) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH_TOKEN_REQUEST_FAILED)
    }
    
    @Test
    fun `OAuth 탈퇴 실패 - 유효하지 않은 인증 코드인 경우 예외를 던진다`() {
        // given
        val provider = Member.OAuthProvider.GOOGLE
        val code = "invalid-auth-code"
        
        // when & then
        assertThatThrownBy { sut.withdraw(provider, code) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH_TOKEN_REQUEST_FAILED)
    }
} 