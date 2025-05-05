package store.fillsa.fillsa_api.domain.auth.service.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisTokenService(
    private val redisTemplate: StringRedisTemplate
) {
    fun createTempToken(tempToken: String, memberSeq: Long, ttlMillis: Long) {
        redisTemplate.opsForValue().set("temp_token:$tempToken", memberSeq.toString(), ttlMillis, TimeUnit.MILLISECONDS)
    }

    fun getAndDeleteTempToken(tempToken: String): String? {
        val code = redisTemplate.opsForValue().get("temp_token:$tempToken")
        redisTemplate.delete("temp_token:$tempToken")
        return code
    }

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