package store.fillsa.fillsa_api.domain.quote.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.assertj.core.api.Assertions.*
import org.mockito.kotlin.any
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.redis.service.DailyQuoteCacheService
import store.fillsa.fillsa_api.fixture.quote.persist.QuotePersistFactory
import store.fillsa.fillsa_api.fixture.quote.entity.QuoteEntityFactory
import java.time.LocalDate
import java.time.YearMonth

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuoteServiceTest @Autowired constructor(
    private val sut: QuoteService,
    private val quotePersistFactory: QuotePersistFactory
) {

    @MockkBean
    private lateinit var dailyQuoteCacheService: DailyQuoteCacheService
    
    @Test
    fun `일일 명언 조회 성공 - 해당 날짜의 명언을 반환한다`() {
        // given
        val quoteDate = LocalDate.of(2024, 1, 1)
        val quote = QuoteEntityFactory.quote(
            korQuote = "한국어 명언",
            korAuthor = "한국 작가",
            engAuthor = "English Author"
        )
        val dailyQuote = QuoteEntityFactory.dailyQuote(
            quoteDate = quoteDate
        )
        val (_, savedDailyQuote) = quotePersistFactory.createQuoteWithDailyQuote(quote, dailyQuote)

        every { dailyQuoteCacheService.getDailyQuote(any()) } returns null
        every { dailyQuoteCacheService.cacheDailyQuote(any()) } returns any()
        
        // when
        val result = sut.getDailyQuote(quoteDate)
        
        // then
        assertThat(result.dailyQuoteSeq).isEqualTo(savedDailyQuote.dailyQuoteSeq)
        assertThat(result.korQuote).isEqualTo(quote.korQuote)
        assertThat(result.korAuthor).isEqualTo(quote.korAuthor)
        assertThat(result.engAuthor).isEqualTo(quote.engAuthor)
    }
    
    @Test
    fun `일일 명언 조회 실패 - 해당 날짜의 명언이 없는 경우 예외를 던진다`() {
        // given
        val quoteDate = LocalDate.of(2024, 1, 1)

        every { dailyQuoteCacheService.getDailyQuote(any()) } returns null
        every { dailyQuoteCacheService.cacheDailyQuote(any()) } returns any()
        
        // when & then
        assertThatThrownBy { sut.getDailyQuote(quoteDate) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
    
    @Test
    fun `월별 명언 조회 성공 - 해당 월의 명언 목록을 반환한다`() {
        // given
        val yearMonth = YearMonth.of(2024, 1)
        val quote1 = QuoteEntityFactory.quote(
            korQuote = "한국어 명언 1",
            korAuthor = "한국 작가 1"
        )
        val dailyQuote1 = QuoteEntityFactory.dailyQuote(
            quoteDate = LocalDate.of(2024, 1, 1)
        )
        val (_, savedDailyQuote1) = quotePersistFactory.createQuoteWithDailyQuote(quote1, dailyQuote1)
        
        val quote2 = QuoteEntityFactory.quote(
            korQuote = "한국어 명언 2", 
            korAuthor = "한국 작가 2"
        )
        val dailyQuote2 = QuoteEntityFactory.dailyQuote(
            quoteDate = LocalDate.of(2024, 1, 2)
        )
        val (_, savedDailyQuote2) = quotePersistFactory.createQuoteWithDailyQuote(quote2, dailyQuote2)

        every { dailyQuoteCacheService.getMonthlyQuotes(any(), any()) } returns emptyList()
        every { dailyQuoteCacheService.cacheMonthlyQuotes(any()) } returns any()
        
        // when
        val result = sut.monthlyQuotes(yearMonth)
        
        // then
        assertThat(result).hasSize(2)
        
        val firstResult = result.find { it.quoteDate == LocalDate.of(2024, 1, 1) }!!
        assertThat(firstResult.dailyQuoteSeq).isEqualTo(savedDailyQuote1.dailyQuoteSeq)
        assertThat(firstResult.quote).isEqualTo(quote1.korQuote)
        assertThat(firstResult.author).isEqualTo(quote1.korAuthor)
        
        val secondResult = result.find { it.quoteDate == LocalDate.of(2024, 1, 2) }!!
        assertThat(secondResult.dailyQuoteSeq).isEqualTo(savedDailyQuote2.dailyQuoteSeq)
        assertThat(secondResult.quote).isEqualTo(quote2.korQuote)
        assertThat(secondResult.author).isEqualTo(quote2.korAuthor)
    }
    
    @Test
    fun `월별 명언 조회 성공 - 해당 월에 명언이 없는 경우 빈 목록을 반환한다`() {
        // given
        val yearMonth = YearMonth.of(2024, 1)

        every { dailyQuoteCacheService.getMonthlyQuotes(any(), any()) } returns emptyList()
        every { dailyQuoteCacheService.cacheMonthlyQuotes(any()) } returns any()
        
        // when
        val result = sut.monthlyQuotes(yearMonth)
        
        // then
        assertThat(result).isEmpty()
    }
} 