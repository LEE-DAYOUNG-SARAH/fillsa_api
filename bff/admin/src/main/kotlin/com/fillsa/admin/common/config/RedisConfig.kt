package com.fillsa.admin.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * bff:app 의 RedisConfig 와 동일한 구성. Lettuce 는 커넥션을 지연 연결하므로
 * Redis 가 떠 있지 않아도 애플리케이션/테스트 컨텍스트 기동에는 영향이 없다.
 */
@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(redisConnectionFactory)
    }
}
