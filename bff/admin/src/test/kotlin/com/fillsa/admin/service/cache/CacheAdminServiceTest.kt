package com.fillsa.admin.service.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fillsa.service.quote.DailyQuote
import com.fillsa.service.quote.DailyQuoteRepository
import com.fillsa.service.quote.Quote
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import java.time.LocalDate

/**
 * 라이브 Redis 없이 검증하는 순수 MockK 단위 테스트.
 * StringRedisTemplate/ZSetOperations 를 모두 mockk() 로 대체하므로 Spring 컨텍스트나
 * 실제 Redis 커넥션이 전혀 필요 없다 — @SpringBootTest + @MockkBean 대신 이 방식을 택한 이유는
 * ZSetOperations 체이닝(opsForZSet().add/range/removeRangeByScore)을 세밀하게 verify 하면서도
 * 컨텍스트 기동 비용 없이 빠르게 돌리기 위함이다.
 */
class CacheAdminServiceTest {

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var zSetOperations: ZSetOperations<String, String>
    private lateinit var dailyQuoteRepository: DailyQuoteRepository
    private lateinit var objectMapper: ObjectMapper
    private lateinit var sut: CacheAdminService

    companion object {
        private const val CACHE_KEY = "daily_quotes_sorted"
    }

    @BeforeEach
    fun setUp() {
        redisTemplate = mockk()
        zSetOperations = mockk()
        dailyQuoteRepository = mockk()
        objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        every { redisTemplate.opsForZSet() } returns zSetOperations

        sut = CacheAdminService(redisTemplate, dailyQuoteRepository, objectMapper)
    }

    private fun dailyQuote(date: LocalDate, korQuote: String = "명언"): DailyQuote {
        val quote = Quote(quoteSeq = 1L, korQuote = korQuote, korAuthor = "작가")
        return DailyQuote(dailyQuoteSeq = 1L, quote = quote, quoteDate = date, quoteDayOfWeek = "월")
    }

    @Test
    fun `미리보기 - ZSET 전체 범위를 조회해 반환한다`() {
        val today = LocalDate.now()
        val record = DailyQuoteCacheRecord.from(dailyQuote(today))
        val json = objectMapper.writeValueAsString(record)
        every { zSetOperations.range(CACHE_KEY, 0, -1) } returns linkedSetOf(json)

        val result = sut.preview()

        assertThat(result.count).isEqualTo(1)
        assertThat(result.entries[0].date).isEqualTo(today)
        assertThat(result.entries[0].korQuote).isEqualTo(record.korQuote)
        verify(exactly = 1) { zSetOperations.range(CACHE_KEY, 0, -1) }
    }

    @Test
    fun `미리보기 - 캐시가 비어있으면 빈 목록을 반환한다`() {
        every { zSetOperations.range(CACHE_KEY, 0, -1) } returns null

        val result = sut.preview()

        assertThat(result.count).isEqualTo(0)
        assertThat(result.entries).isEmpty()
    }

    @Test
    fun `갱신 - date 없으면 캐시 전체 삭제 후 DB 전체로 다시 채운다`() {
        val quotes = listOf(dailyQuote(LocalDate.now()), dailyQuote(LocalDate.now().plusDays(1)))
        every { redisTemplate.delete(CACHE_KEY) } returns true
        every { dailyQuoteRepository.findAllOrderByQuoteDateAsc() } returns quotes
        every { zSetOperations.add(CACHE_KEY, any(), any()) } returns true

        val result = sut.refresh(CacheRefreshRequest(date = null))

        assertThat(result.refreshedCount).isEqualTo(2)
        verify(exactly = 1) { redisTemplate.delete(CACHE_KEY) }
        verify(exactly = 2) { zSetOperations.add(CACHE_KEY, any(), any()) }
    }

    @Test
    fun `갱신 - date 지정 시 해당 날짜 하나만 삭제 후 재적재한다`() {
        val date = LocalDate.now()
        val quote = dailyQuote(date)
        val score = date.toEpochDay().toDouble()
        every { dailyQuoteRepository.findByQuoteDate(date) } returns quote
        every { zSetOperations.removeRangeByScore(CACHE_KEY, score, score) } returns 1L
        every { zSetOperations.add(CACHE_KEY, any(), score) } returns true

        val result = sut.refresh(CacheRefreshRequest(date = date))

        assertThat(result.refreshedCount).isEqualTo(1)
        verify(exactly = 1) { zSetOperations.removeRangeByScore(CACHE_KEY, score, score) }
        verify(exactly = 1) { zSetOperations.add(CACHE_KEY, any(), score) }
        verify(exactly = 0) { redisTemplate.delete(any<String>()) }
    }

    @Test
    fun `갱신 - date 지정했지만 해당 날짜 배정이 없으면 404(NOT_FOUND)`() {
        val date = LocalDate.now()
        every { dailyQuoteRepository.findByQuoteDate(date) } returns null

        assertThatThrownBy { sut.refresh(CacheRefreshRequest(date = date)) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제 - 캐시 키를 삭제한다`() {
        every { redisTemplate.delete(CACHE_KEY) } returns true

        sut.evict()

        verify(exactly = 1) { redisTemplate.delete(CACHE_KEY) }
    }
}
