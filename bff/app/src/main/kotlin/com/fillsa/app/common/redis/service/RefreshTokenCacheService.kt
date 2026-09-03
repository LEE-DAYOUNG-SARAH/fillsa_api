package com.fillsa.app.common.redis.service

import org.springframework.stereotype.Service
import com.fillsa.app.common.redis.entity.RefreshTokenCache
import com.fillsa.app.common.redis.repository.RefreshTokenCacheRepository

@Service
class RefreshTokenCacheService(
    private val refreshTokenCacheRepository: RefreshTokenCacheRepository
) {
    fun createRefreshToken(memberId: Long, deviceId: String, refreshToken: String, ttlMillis: Long) {
        refreshTokenCacheRepository.save(
            RefreshTokenCache.from(memberId, deviceId, refreshToken, ttlMillis)
        )
    }

    /**
     * 저장된 리프레시 토큰과 일치하는지 확인한다.
     *
     * 이 검증이 없으면 로그아웃이 실제로 무효화되지 않는다.
     * JWT 서명·만료만 보면, 로그아웃으로 Redis 에서 지운 토큰도 유효기간(90일) 내내 재발급에 쓸 수 있다.
     */
    fun isValidRefreshToken(memberId: Long, deviceId: String, refreshToken: String): Boolean {
        val cached = refreshTokenCacheRepository
            .findById(RefreshTokenCache.createId(memberId, deviceId))
            .orElse(null) ?: return false

        return cached.token == refreshToken
    }

    fun deleteRefreshTokenForLogout(memberId: Long, deviceId: String) {
        refreshTokenCacheRepository.deleteById(RefreshTokenCache.createId(memberId, deviceId))
    }

    fun deleteRefreshTokenForWithdrawal(memberId: Long) {
        refreshTokenCacheRepository.deleteByMemberId(memberId)
    }
}