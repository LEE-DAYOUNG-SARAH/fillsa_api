package store.fillsa.fillsa_api.domain.auth.service.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisTokenService(
    private val redisTemplate: StringRedisTemplate
) {
    fun createRefreshToken(memberId: Long, deviceId: String, refreshToken: String, ttlMillis: Long) {
        redisTemplate.opsForValue().set("refresh:$memberId:$deviceId", refreshToken, ttlMillis, TimeUnit.MILLISECONDS)
    }

    fun getRefreshToken(memberId: Long, deviceId: String): String? =
        redisTemplate.opsForValue().get("refresh:$memberId:$deviceId")

    fun deleteRefreshToken(memberId: Long, deviceId: String) {
        redisTemplate.delete("refresh:$memberId:$deviceId")
    }

    fun validateRefreshToken(memberSeq: Long, deviceId: String, refreshToken: String): Boolean {
        val redisRefreshToken = getRefreshToken(memberSeq, deviceId)
        return redisRefreshToken != null && redisRefreshToken == refreshToken
    }
}