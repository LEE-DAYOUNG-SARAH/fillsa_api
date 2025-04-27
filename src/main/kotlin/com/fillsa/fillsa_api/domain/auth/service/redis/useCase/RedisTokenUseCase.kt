package com.fillsa.fillsa_api.domain.auth.service.redis.useCase

interface RedisTokenUseCase {
    /**
     * 임시 토큰 저장 (TTL 적용)
     */
    fun createTempToken(tempToken: String, memberSeq: Long, ttlMillis: Long)

    /**
     * 임시 토큰 조회 및 삭제 (1회성)
     */
    fun getAndDeleteTempToken(tempToken: String): String?

    /**
     * 리프레시 토큰 저장 (TTL 적용)
     */
    fun createRefreshToken(memberId: Long, deviceId: String, refreshToken: String, ttlMillis: Long)

    /**
     * 리프레시 토큰 조회
     */
    fun getRefreshToken(memberId: Long, deviceId: String): String?

    /**
     * 리프레시 토큰 삭제
     */
    fun deleteRefreshToken(memberId: Long, deviceId: String)

    /**
     *  리프레시 검증
     */
    fun validateRefreshToken(memberSeq: Long, deviceId: String, refreshToken: String): Boolean
}