package store.fillsa.fillsa_api.domain.auth.service.redis

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.assertj.core.api.Assertions.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RedisTokenServiceTest @Autowired constructor(
    private val redisTemplate: StringRedisTemplate,
    private val sut: RedisTokenService
) {
    
    @Test
    fun `리프레시 토큰 생성 성공 - Redis에 토큰을 저장한다`() {
        // given
        val memberId = 1L
        val deviceId = "test-device-id"
        val refreshToken = "test-refresh-token"
        val ttlMillis = 86400000L
        val expectedKey = "refresh:$memberId:$deviceId"
        
        // when
        sut.createRefreshToken(memberId, deviceId, refreshToken, ttlMillis)
        
        // then
        val storedToken = redisTemplate.opsForValue().get(expectedKey)
        assertThat(storedToken).isEqualTo(refreshToken)
        
        val ttl = redisTemplate.getExpire(expectedKey, TimeUnit.MILLISECONDS)
        assertThat(ttl).isPositive()
    }
    
    @Test
    fun `로그아웃용 리프레시 토큰 삭제 성공 - Redis에서 특정 디바이스의 토큰을 삭제한다`() {
        // given
        val memberId = 1L
        val deviceId = "test-device-id"
        val refreshToken = "test-refresh-token"
        val expectedKey = "refresh:$memberId:$deviceId"
        
        sut.createRefreshToken(memberId, deviceId, refreshToken, 86400000L)
        
        // when
        sut.deleteRefreshTokenForLogout(memberId, deviceId)
        
        // then
        val deletedToken = redisTemplate.opsForValue().get(expectedKey)
        assertThat(deletedToken).isNull()
    }
    
    @Test
    fun `탈퇴용 리프레시 토큰 삭제 성공 - Redis에서 회원의 모든 토큰을 삭제한다`() {
        // given
        val memberId = 1L
        val pattern = "refresh:$memberId:*"
        
        sut.createRefreshToken(memberId, "device1", "token1", 86400000L)
        sut.createRefreshToken(memberId, "device2", "token2", 86400000L)
        
        // when
        sut.deleteRefreshTokenForWithdrawal(memberId)
        
        // then
        val remainingKeys = redisTemplate.keys(pattern)
        assertThat(remainingKeys).isEmpty()
    }
    
    @Test
    fun `탈퇴용 리프레시 토큰 삭제 성공 - 삭제할 토큰이 없는 경우 아무것도 하지 않는다`() {
        // given
        val memberId = 999L
        val pattern = "refresh:$memberId:*"
        
        // when
        sut.deleteRefreshTokenForWithdrawal(memberId)
        
        // then
        val keys = redisTemplate.keys(pattern)
        assertThat(keys).isEmpty()
    }
    
    @Test
    fun `탈퇴용 리프레시 토큰 삭제 성공 - keys 결과가 빈 집합인 경우 아무것도 하지 않는다`() {
        // given
        val memberId = 998L
        val pattern = "refresh:$memberId:*"
        
        // when
        sut.deleteRefreshTokenForWithdrawal(memberId)
        
        // then
        val keys = redisTemplate.keys(pattern)
        assertThat(keys).isEmpty()
    }
    
    @Test
    fun `리프레시 토큰 생성 성공 - 다른 TTL 값으로도 정상 동작한다`() {
        // given
        val memberId = 2L
        val deviceId = "another-device-id"
        val refreshToken = "another-refresh-token"
        val ttlMillis = 3600000L
        val expectedKey = "refresh:$memberId:$deviceId"
        
        // when
        sut.createRefreshToken(memberId, deviceId, refreshToken, ttlMillis)
        
        // then
        val storedToken = redisTemplate.opsForValue().get(expectedKey)
        assertThat(storedToken).isEqualTo(refreshToken)
        
        val ttl = redisTemplate.getExpire(expectedKey, TimeUnit.MILLISECONDS)
        assertThat(ttl).isPositive()
    }
    
    @Test
    fun `리프레시 토큰 생성 성공 - 긴 디바이스 ID로도 정상 동작한다`() {
        // given
        val memberId = 1L
        val deviceId = "very-long-device-id-with-many-characters-1234567890"
        val refreshToken = "test-refresh-token"
        val ttlMillis = 86400000L
        val expectedKey = "refresh:$memberId:$deviceId"
        
        // when
        sut.createRefreshToken(memberId, deviceId, refreshToken, ttlMillis)
        
        // then
        val storedToken = redisTemplate.opsForValue().get(expectedKey)
        assertThat(storedToken).isEqualTo(refreshToken)
        
        val ttl = redisTemplate.getExpire(expectedKey, TimeUnit.MILLISECONDS)
        assertThat(ttl).isPositive()
    }
} 