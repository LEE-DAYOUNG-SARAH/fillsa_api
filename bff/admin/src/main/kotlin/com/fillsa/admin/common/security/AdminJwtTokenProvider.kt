package com.fillsa.admin.common.security

import com.fillsa.service.admin.AdminRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

/**
 * 어드민 전용 JWT 발급/검증기.
 *
 * 토큰 분리(TOKEN SEPARATION):
 *  - 앱(bff:app) 토큰과 완전히 다른 서명 키(admin-jwt.secret)를 사용한다.
 *    → 앱 토큰은 이 키로 검증 시 서명 불일치로 거부된다.
 *  - 추가 방어선으로 tokenType=ADMIN 클레임을 심고, 검증 시 이를 확인한다.
 *    → 설령 키가 같더라도 tokenType 이 없는 앱 토큰은 어드민 인증에 사용될 수 없다.
 */
@Component
class AdminJwtTokenProvider(
    @Value("\${admin-jwt.secret}")
    secretKey: String,
    @Value("\${admin-jwt.access-token-validity}")
    val accessTokenValidity: Long,
    @Value("\${admin-jwt.refresh-token-validity}")
    val refreshTokenValidity: Long,
) {
    private val key: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))

    companion object {
        const val TOKEN_TYPE_CLAIM = "tokenType"
        const val TOKEN_TYPE_ADMIN = "ADMIN"
        const val ROLE_CLAIM = "role"
        const val TOKEN_KIND_CLAIM = "kind"
        const val KIND_ACCESS = "ACCESS"
        const val KIND_REFRESH = "REFRESH"
    }

    fun createAccessToken(adminSeq: Long, role: AdminRole): String =
        buildToken(adminSeq, role, accessTokenValidity, KIND_ACCESS)

    fun createRefreshToken(adminSeq: Long, role: AdminRole): String =
        buildToken(adminSeq, role, refreshTokenValidity, KIND_REFRESH)

    private fun buildToken(adminSeq: Long, role: AdminRole, validityMillis: Long, kind: String): String {
        val now = Date()
        return Jwts.builder()
            .setSubject(adminSeq.toString())
            .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ADMIN)
            .claim(ROLE_CLAIM, role.name)
            .claim(TOKEN_KIND_CLAIM, kind)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + validityMillis))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /** 서명·만료 검증 후 클레임 반환. 예외는 호출측에서 처리한다. */
    fun parseClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

    /** 어드민 액세스 토큰인지 확인 (tokenType=ADMIN). */
    fun isAdminToken(claims: Claims): Boolean =
        claims[TOKEN_TYPE_CLAIM] == TOKEN_TYPE_ADMIN

    fun isRefreshToken(claims: Claims): Boolean =
        claims[TOKEN_KIND_CLAIM] == KIND_REFRESH

    fun getAdminSeq(claims: Claims): Long = claims.subject.toLong()

    fun getRole(claims: Claims): AdminRole = AdminRole.valueOf(claims[ROLE_CLAIM] as String)
}
