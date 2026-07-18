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

    fun deleteRefreshTokenForLogout(memberId: Long, deviceId: String) {
        refreshTokenCacheRepository.deleteById(RefreshTokenCache.createId(memberId, deviceId))
    }

    fun deleteRefreshTokenForWithdrawal(memberId: Long) {
        refreshTokenCacheRepository.deleteByMemberId(memberId)
    }
}