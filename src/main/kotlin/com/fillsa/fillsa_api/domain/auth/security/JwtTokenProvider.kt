package com.fillsa.fillsa_api.domain.auth.security

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
    private val secretKey: String,

    @Value("\${jwt.access-token-validity}")
    private val accessTokenValidity: Long,

    @Value("\${jwt.refresh-token-validity}")
    private val refreshTokenValidity: Long
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
            refreshToken = refreshToken,
            expiresIn = accessTokenExpiresIn / 1000 // 밀리초를 초로 변환
        )
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
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