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

    fun deleteRefreshTokenForLogout(memberId: Long, deviceId: String) {
        redisTemplate.delete("refresh:$memberId:$deviceId")
    }

    fun deleteRefreshTokenForWithdrawal(memberId: Long) {
        val keys = redisTemplate.keys("refresh:$memberId:*")
        if (!keys.isNullOrEmpty()) {
            redisTemplate.delete(keys)
        }
    }
}