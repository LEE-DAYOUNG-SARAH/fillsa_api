package com.fillsa.app.common.cache

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class TtlCacheTest {

    private class MutableClock(var now: Instant = Instant.parse("2026-10-20T00:00:00Z")) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = now
    }

    @Test
    fun `TTL 안에서는 로더를 다시 호출하지 않는다`() {
        val clock = MutableClock()
        val sut = TtlCache<String, String>(Duration.ofSeconds(60), clock = clock)
        var calls = 0

        sut.getOrLoad("k") { calls++; "v1" }
        clock.now = clock.now.plusSeconds(59)
        val result = sut.getOrLoad("k") { calls++; "v2" }

        assertThat(result).isEqualTo("v1")
        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun `TTL 이 지나면 다시 로드한다`() {
        val clock = MutableClock()
        val sut = TtlCache<String, String>(Duration.ofSeconds(60), clock = clock)

        sut.getOrLoad("k") { "v1" }
        clock.now = clock.now.plusSeconds(60)

        assertThat(sut.getOrLoad("k") { "v2" }).isEqualTo("v2")
    }

    @Test
    fun `로더가 예외를 던지면 캐시하지 않는다`() {
        val sut = TtlCache<String, String>(Duration.ofSeconds(60))

        runCatching { sut.getOrLoad("k") { throw IllegalStateException() } }

        assertThat(sut.get("k")).isNull()
    }

    @Test
    fun `최대 크기를 넘으면 메모리 상한을 지킨다`() {
        val sut = TtlCache<Int, Int>(Duration.ofMinutes(5), maxSize = 3)

        (1..10).forEach { sut.put(it, it) }

        assertThat(sut.size()).isLessThanOrEqualTo(3)
    }
}
