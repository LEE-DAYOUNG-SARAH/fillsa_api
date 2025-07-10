package store.fillsa.fillsa_api.domain.quote.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.fixture.quote.entity.QuoteEntityFactory
import store.fillsa.fillsa_api.fixture.quote.persist.QuotePersistFactory
import java.time.LocalDate
import java.time.YearMonth

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DailyQuoteServiceTest @Autowired constructor(
    private val quotePersistFactory: QuotePersistFactory,
    private val sut: DailyQuoteService
) {
    
    @Test
    fun `일일 명언 조회 by 시퀀스 성공 - 해당 시퀀스의 명언을 반환한다`() {
        // given
        val quote = QuoteEntityFactory.quote(korQuote = "테스트 명언", korAuthor = "테스트 작가")
        val dailyQuote = QuoteEntityFactory.dailyQuote(
            quote = quote,
            quoteDate = LocalDate.of(2024, 1, 1)
        )
        val (savedQuote, savedDailyQuote) = quotePersistFactory.createQuoteWithDailyQuote(quote, dailyQuote)
        
        // when
        val result = sut.getDailyQuoteByDailQuoteSeq(savedDailyQuote.dailyQuoteSeq)
        
        // then
        assertThat(result).isNotNull
        assertThat(result!!.dailyQuoteSeq).isEqualTo(savedDailyQuote.dailyQuoteSeq)
        assertThat(result.quote.korQuote).isEqualTo(savedQuote.korQuote)
        assertThat(result.quote.korAuthor).isEqualTo(savedQuote.korAuthor)
    }
    
    @Test
    fun `일일 명언 조회 by 시퀀스 실패 - 존재하지 않는 시퀀스인 경우 null을 반환한다`() {
        // given
        val nonExistentSeq = 999L
        
        // when
        val result = sut.getDailyQuoteByDailQuoteSeq(nonExistentSeq)
        
        // then
        assertThat(result).isNull()
    }
    
    @Test
    fun `일일 명언 조회 by 날짜 성공 - 해당 날짜의 명언을 반환한다`() {
        // given
        val quoteDate = LocalDate.of(2024, 1, 15)
        val quote = QuoteEntityFactory.quote(korQuote = "날짜별 명언", korAuthor = "날짜 작가")
        val dailyQuote = QuoteEntityFactory.dailyQuote(
            quote = quote,
            quoteDate = quoteDate
        )
        val (savedQuote, savedDailyQuote) = quotePersistFactory.createQuoteWithDailyQuote(quote, dailyQuote)
        
        // when
        val result = sut.getDailyQuoteByQuoteDate(quoteDate)
        
        // then
        assertThat(result).isNotNull
        assertThat(result!!.quoteDate).isEqualTo(quoteDate)
        assertThat(result.quote.korQuote).isEqualTo(savedQuote.korQuote)
        assertThat(result.quote.korAuthor).isEqualTo(savedQuote.korAuthor)
    }
    
    @Test
    fun `일일 명언 조회 by 날짜 실패 - 존재하지 않는 날짜인 경우 null을 반환한다`() {
        // given
        val nonExistentDate = LocalDate.of(2025, 12, 31)
        
        // when
        val result = sut.getDailyQuoteByQuoteDate(nonExistentDate)
        
        // then
        assertThat(result).isNull()
    }
    
    @Test
    fun `월별 명언 조회 성공 - 과거 월의 명언 목록을 반환한다`() {
        // given
        val yearMonth = YearMonth.of(2023, 12)
        val quote1 = QuoteEntityFactory.quote(korQuote = "12월 명언 1", korAuthor = "12월 작가 1")
        val quote2 = QuoteEntityFactory.quote(korQuote = "12월 명언 2", korAuthor = "12월 작가 2")
        val dailyQuote1 = QuoteEntityFactory.dailyQuote(
            quote = quote1,
            quoteDate = LocalDate.of(2023, 12, 1)
        )
        val dailyQuote2 = QuoteEntityFactory.dailyQuote(
            quote = quote2, 
            quoteDate = LocalDate.of(2023, 12, 15)
        )
        
        quotePersistFactory.createQuoteWithDailyQuote(quote1, dailyQuote1)
        quotePersistFactory.createQuoteWithDailyQuote(quote2, dailyQuote2)
        
        // when
        val result = sut.getDailyQuoteByQuotMonth(yearMonth)
        
        // then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.quote.korQuote }).containsExactlyInAnyOrder("12월 명언 1", "12월 명언 2")
        assertThat(result.map { it.quoteDate }).containsExactlyInAnyOrder(
            LocalDate.of(2023, 12, 1),
            LocalDate.of(2023, 12, 15)
        )
    }
    
    @Test
    fun `월별 명언 조회 성공 - 현재 월의 명언 목록을 반환한다`() {
        // given
        val currentMonth = YearMonth.now()
        val today = LocalDate.now()
        val quote = QuoteEntityFactory.quote(korQuote = "현재 월 명언", korAuthor = "현재 작가")
        val dailyQuote = QuoteEntityFactory.dailyQuote(
            quote = quote,
            quoteDate = today
        )
        
        quotePersistFactory.createQuoteWithDailyQuote(quote, dailyQuote)
        
        // when
        val result = sut.getDailyQuoteByQuotMonth(currentMonth)
        
        // then
        assertThat(result).isNotEmpty
        assertThat(result.any { it.quoteDate == today }).isTrue
        assertThat(result.any { it.quote.korQuote == "현재 월 명언" }).isTrue
    }
    
    @Test
    fun `월별 명언 조회 실패 - 미래 월인 경우 예외를 던진다`() {
        // given
        val futureMonth = YearMonth.now().plusMonths(1)
        
        // when & then
        val exception = assertThrows<BusinessException> {
            sut.getDailyQuoteByQuotMonth(futureMonth)
        }
        
        assertThat(exception.errorCode).isEqualTo(ErrorCode.INVALID_REQUEST)
        assertThat(exception.message).contains("현재 월 이후는 조회할 수 없습니다.")
    }
    
    @Test
    fun `월별 명언 조회 성공 - 해당 월에 명언이 없는 경우 빈 목록을 반환한다`() {
        // given
        val emptyMonth = YearMonth.of(2020, 1)
        
        // when
        val result = sut.getDailyQuoteByQuotMonth(emptyMonth)
        
        // then
        assertThat(result).isEmpty()
    }
    
    @Test
    fun `일일 명언 조회 성공 - 동일한 날짜에 하나의 명언만 존재한다`() {
        // given
        val specificDate = LocalDate.of(2024, 2, 14)
        val quote = QuoteEntityFactory.quote(korQuote = "발렌타인 명언", korAuthor = "사랑 작가")
        val dailyQuote = QuoteEntityFactory.dailyQuote(
            quote = quote,
            quoteDate = specificDate
        )
        
        quotePersistFactory.createQuoteWithDailyQuote(quote, dailyQuote)
        
        // when
        val result = sut.getDailyQuoteByQuoteDate(specificDate)
        
        // then
        assertThat(result).isNotNull
        assertThat(result!!.quoteDate).isEqualTo(specificDate)
        assertThat(result.quote.korQuote).isEqualTo("발렌타인 명언")
    }
    
    @Test
    fun `월별 명언 조회 성공 - 월의 첫날과 마지막날 경계값 테스트`() {
        // given
        val testMonth = YearMonth.of(2024, 3)
        val firstDay = testMonth.atDay(1)
        val lastDay = testMonth.atEndOfMonth()
        
        val quote1 = QuoteEntityFactory.quote(korQuote = "3월 첫날", korAuthor = "시작 작가")
        val quote2 = QuoteEntityFactory.quote(korQuote = "3월 마지막날", korAuthor = "끝 작가")
        val dailyQuote1 = QuoteEntityFactory.dailyQuote(quote = quote1, quoteDate = firstDay)
        val dailyQuote2 = QuoteEntityFactory.dailyQuote(quote = quote2, quoteDate = lastDay)
        
        quotePersistFactory.createQuoteWithDailyQuote(quote1, dailyQuote1)
        quotePersistFactory.createQuoteWithDailyQuote(quote2, dailyQuote2)
        
        // when
        val result = sut.getDailyQuoteByQuotMonth(testMonth)
        
        // then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.quoteDate }).containsExactlyInAnyOrder(firstDay, lastDay)
        assertThat(result.map { it.quote.korQuote }).containsExactlyInAnyOrder("3월 첫날", "3월 마지막날")
    }
} 