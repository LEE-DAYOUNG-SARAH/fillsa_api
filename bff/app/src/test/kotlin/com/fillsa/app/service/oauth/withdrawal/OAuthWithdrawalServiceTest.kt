package com.fillsa.app.service.oauth.withdrawal

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import com.fillsa.service.member.Member

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