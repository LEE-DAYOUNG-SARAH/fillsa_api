package store.fillsa.fillsa_api.common.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    val secretKey: String,
    @Value("\${jwt.access-token-validity}")
    val accessTokenValidity: Long,
    @Value("\${jwt.refresh-token-validity}")
    val refreshTokenValidity: Long
) {
    lateinit var key: Key

    init {
        val keyBytes = Decoders.BASE64.decode(secretKey)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    fun createTokens(memberSeq: Long): TokenInfo {
        val now = Date()
        val accessTokenExpiresIn = accessTokenValidity
        val refreshTokenExpiresIn = refreshTokenValidity

        // 액세스 토큰 생성
        val accessToken = Jwts.builder()
            .setSubject(memberSeq.toString())
            .setIssuedAt(now)
            .setExpiration(Date(now.time + accessTokenExpiresIn))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        // 리프레시 토큰 생성
        val refreshToken = Jwts.builder()
            .setSubject(memberSeq.toString())
            .setIssuedAt(now)
            .setExpiration(Date(now.time + refreshTokenExpiresIn))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        return TokenInfo(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun validateToken(token: String): Boolean {
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)

        return true
    }

    fun getMemberSeqFromToken(token: String): Long {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
            .toLong()
    }
}