package com.fillsa.fillsa_api.domain.auth.service.redis

import com.fillsa.fillsa_api.domain.auth.service.redis.useCase.RedisTokenUseCase
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisTokenService(
    private val redisTemplate: StringRedisTemplate
): RedisTokenUseCase {
    override fun createTempToken(tempToken: String, memberSeq: Long, ttlMillis: Long) {
        redisTemplate.opsForValue().set("temp_token:$tempToken", memberSeq.toString(), ttlMillis, TimeUnit.MILLISECONDS)
    }

    override fun getAndDeleteTempToken(tempToken: String): String? {
        val code = redisTemplate.opsForValue().get("temp_token:$tempToken")
        redisTemplate.delete("temp_token:$tempToken")
        return code
    }

    override fun createRefreshToken(memberId: Long, deviceId: String, refreshToken: String, ttlMillis: Long) {
        redisTemplate.opsForValue().set("refresh:$memberId:$deviceId", refreshToken, ttlMillis, TimeUnit.MILLISECONDS)
    }

    override fun getRefreshToken(memberId: Long, deviceId: String): String? =
        redisTemplate.opsForValue().get("refresh:$memberId:$deviceId")

    override fun deleteRefreshToken(memberId: Long, deviceId: String) {
        redisTemplate.delete("refresh:$memberId:$deviceId")
    }

    override fun validateRefreshToken(memberSeq: Long, deviceId: String, refreshToken: String): Boolean {
        val redisRefreshToken = getRefreshToken(memberSeq, deviceId)
        return redisRefreshToken != null && redisRefreshToken == refreshToken
    }
}