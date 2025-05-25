package store.fillsa.fillsa_api.common.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class JwtTokenProviderTest @Autowired constructor(
    val sut: JwtTokenProvider
) {
    @Test
    fun `토큰 발급용`() {
        val memberSeq = 1L
        val tokenInfo = sut.createTokens(memberSeq)
        println(tokenInfo)
    }
}