package com.fillsa.admin.security

import com.fillsa.admin.common.security.AdminJwtTokenProvider
import com.fillsa.admin.fixture.AdminFixtures
import com.fillsa.service.admin.AdminRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.Date

/**
 * 토큰 분리 검증: 앱 회원 토큰(다른 서명 키 + tokenType 없음)으로는
 * 어드민 보호 엔드포인트에 인증할 수 없어야 한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminTokenSeparationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val adminJwtTokenProvider: AdminJwtTokenProvider,
    private val adminFixtures: AdminFixtures,
) {
    // 앱(bff:app) 스타일 토큰을 흉내내기 위한 별도 키 (admin-jwt.secret 과 다름)
    @Value("\${app-jwt.secret}")
    private lateinit var appSecret: String

    private val protectedUrl = "/api/admin/v1/daily-quotes?yearMonth=2026-06"

    @Test
    fun `토큰 없이 보호 엔드포인트 접근 시 401`() {
        mockMvc.get(protectedUrl)
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `유효한 어드민 토큰으로는 접근 가능(200)`() {
        val admin = adminFixtures.admin()
        val token = adminJwtTokenProvider.createAccessToken(admin.adminSeq, AdminRole.ADMIN)

        mockMvc.get(protectedUrl) {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `앱 회원 토큰(다른 키, tokenType 없음)으로는 어드민 엔드포인트 인증 실패(401)`() {
        val appKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(appSecret))
        val now = Date()
        // 앱 토큰: subject 만 담고 tokenType/role 클레임 없음, 앱 키로 서명
        val appToken = Jwts.builder()
            .setSubject("1")
            .setIssuedAt(now)
            .setExpiration(Date(now.time + 3_600_000))
            .signWith(appKey, SignatureAlgorithm.HS256)
            .compact()

        mockMvc.get(protectedUrl) {
            header("Authorization", "Bearer $appToken")
        }.andExpect { status { isUnauthorized() } }
    }
}
