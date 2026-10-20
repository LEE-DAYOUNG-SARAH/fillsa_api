package com.fillsa.app.common.cache

import java.time.Clock
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * 프로세스 메모리 TTL 캐시.
 *
 * 왜 Redis 가 아니라 메모리인가 — 앱 서버는 컨테이너 1개라 인스턴스 간 정합성 문제가 없고,
 * 이 캐시의 목적은 "요청마다 TiDB 왕복(트랜잭션 1회 ≈ 0.5초)을 없애는 것"이라 네트워크를 타지 않는 쪽이 맞다.
 *
 * 만료된 항목은 조회 시 지운다. 크기가 [maxSize] 를 넘으면 만료 항목을 정리하고,
 * 그래도 넘치면 전부 비운다(LRU 를 구현하지 않는 대신 메모리 상한만 보장).
 */
class TtlCache<K : Any, V : Any>(
    private val ttl: Duration,
    private val maxSize: Int = 10_000,
    private val clock: Clock = Clock.systemUTC(),
) {
    private data class Entry<V>(val value: V, val expiresAtMillis: Long)

    private val store = ConcurrentHashMap<K, Entry<V>>()

    fun get(key: K): V? {
        val entry = store[key] ?: return null
        if (entry.expiresAtMillis <= clock.millis()) {
            store.remove(key, entry)
            return null
        }
        return entry.value
    }

    fun put(key: K, value: V) {
        if (store.size >= maxSize) {
            evictExpired()
            if (store.size >= maxSize) store.clear()
        }
        store[key] = Entry(value, clock.millis() + ttl.toMillis())
    }

    /** 캐시에 없거나 만료됐으면 [loader] 로 읽어 채운다. [loader] 가 예외를 던지면 캐시하지 않는다. */
    fun getOrLoad(key: K, loader: () -> V): V =
        get(key) ?: loader().also { put(key, it) }

    fun evict(key: K) {
        store.remove(key)
    }

    fun clear() = store.clear()

    fun size(): Int = store.size

    private fun evictExpired() {
        val now = clock.millis()
        store.entries.removeIf { it.value.expiresAtMillis <= now }
    }
}
