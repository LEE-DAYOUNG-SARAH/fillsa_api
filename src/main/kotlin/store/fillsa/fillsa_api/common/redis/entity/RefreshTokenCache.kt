package store.fillsa.fillsa_api.common.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash("refresh")
data class RefreshTokenCache(
    @Id
    val id: String,                 // "$memberId:$deviceId" 같은 복합키 문자열

    @Indexed
    val memberId: Long,             // 회원 탈퇴 시 일괄 삭제용 인덱스

    @Indexed
    val deviceId: String,           // 디바이스 단일 삭제용 인덱스(옵션)

    val token: String,

    @TimeToLive
    val ttlSeconds: Long    // TTL in seconds
) {
    companion object {
        fun createId(memberId: Long, deviceId: String): String = "$memberId:$deviceId"

        fun from(memberId: Long, deviceId: String, token: String, ttlMillis: Long) = RefreshTokenCache(
            id = createId(memberId, deviceId),
            memberId = memberId,
            deviceId = deviceId,
            token = token,
            ttlSeconds = ttlMillis / 1000  // Convert milliseconds to seconds
        )
    }
}