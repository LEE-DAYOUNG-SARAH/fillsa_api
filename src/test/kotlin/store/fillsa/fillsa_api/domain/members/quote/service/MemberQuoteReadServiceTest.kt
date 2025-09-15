package store.fillsa.fillsa_api.domain.members.quote.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.redis.service.DailyQuoteCacheService
import store.fillsa.fillsa_api.domain.members.quote.dto.MemberQuotesCommonRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemberQuotesRequest
import store.fillsa.fillsa_api.fixture.member.persist.MemberPersistFactory
import store.fillsa.fillsa_api.fixture.quote.entity.QuoteEntityFactory
import store.fillsa.fillsa_api.fixture.quote.persist.QuotePersistFactory
import java.time.LocalDate
import java.time.YearMonth

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberQuoteReadServiceTest @Autowired constructor(
    private val memberPersistFactory: MemberPersistFactory,
    private val quotePersistFactory: QuotePersistFactory,
    private val sut: MemberQuoteReadService
) {

    @MockkBean
    private lateinit var dailyQuoteCacheService: DailyQuoteCacheService

    
    @Test
    fun `일일 명언 조회 성공 - 회원 명언이 있는 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val quoteDate = LocalDate.of(2024, 1, 1)
        val quote = QuoteEntityFactory.quote(
            korQuote = "한국어 명언",
            korAuthor = "한국 작가", 
            engAuthor = "English Author"
        )
        val dailyQuote = QuoteEntityFactory.dailyQuote(
            quote = quote,
            quoteDate = quoteDate
        )
        val (savedQuote, savedDailyQuote) = quotePersistFactory.createQuoteWithDailyQuote(quote, dailyQuote)
        val memberQuote = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote
            )
        )

        every { dailyQuoteCacheService.getDailyQuote(any()) } returns null
        every { dailyQuoteCacheService.cacheDailyQuote(any()) } returns any()

        
        // when
        val result = sut.dailyQuote(member, quoteDate)
        
        // then
        assertThat(result.dailyQuoteSeq).isEqualTo(savedDailyQuote.dailyQuoteSeq)
        assertThat(result.korQuote).isEqualTo(savedQuote.korQuote)
        assertThat(result.korAuthor).isEqualTo(savedQuote.korAuthor)
        assertThat(result.engAuthor).isEqualTo(savedQuote.engAuthor)
    }
    
    @Test
    fun `일일 명언 조회 성공 - 회원 명언이 없는 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val quoteDate = LocalDate.of(2024, 1, 1)
        val quote = QuoteEntityFactory.quote(
            korQuote = "한국어 명언",
            korAuthor = "한국 작가",
            engAuthor = "English Author"
        )
        val dailyQuote = QuoteEntityFactory.dailyQuote(
            quote = quote,
            quoteDate = quoteDate
        )
        val (savedQuote, savedDailyQuote) = quotePersistFactory.createQuoteWithDailyQuote(quote, dailyQuote)

        every { dailyQuoteCacheService.getDailyQuote(any()) } returns null
        every { dailyQuoteCacheService.cacheDailyQuote(any()) } returns any()

        // when
        val result = sut.dailyQuote(member, quoteDate)
        
        // then
        assertThat(result.dailyQuoteSeq).isEqualTo(savedDailyQuote.dailyQuoteSeq)
        assertThat(result.korQuote).isEqualTo(savedQuote.korQuote)
        assertThat(result.korAuthor).isEqualTo(savedQuote.korAuthor)
        assertThat(result.engAuthor).isEqualTo(savedQuote.engAuthor)
    }
    
    @Test
    fun `일일 명언 조회 실패 - 존재하지 않는 날짜인 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val nonExistentQuoteDate = LocalDate.of(2025, 12, 31) // Future date

        every { dailyQuoteCacheService.getDailyQuote(any()) } returns null
        every { dailyQuoteCacheService.cacheDailyQuote(any()) } returns any()
        
        // when & then
        assertThatThrownBy { sut.dailyQuote(member, nonExistentQuoteDate) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
    
    @Test
    fun `월별 명언 조회 성공 - 회원 명언이 있는 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val yearMonth = YearMonth.of(2024, 1)
        val quote1 = QuoteEntityFactory.quote(
            korQuote = "한국어 명언 1",
            korAuthor = "한국 작가 1"
        )
        val dailyQuote1 = QuoteEntityFactory.dailyQuote(
            quote = quote1,
            quoteDate = LocalDate.of(2024, 1, 1)
        )
        val (savedQuote1, savedDailyQuote1) = quotePersistFactory.createQuoteWithDailyQuote(quote1, dailyQuote1)
        
        val quote2 = QuoteEntityFactory.quote(
            korQuote = "한국어 명언 2",
            korAuthor = "한국 작가 2"
        )
        val dailyQuote2 = QuoteEntityFactory.dailyQuote(
            quote = quote2,
            quoteDate = LocalDate.of(2024, 1, 2)
        )
        val (savedQuote2, savedDailyQuote2) = quotePersistFactory.createQuoteWithDailyQuote(quote2, dailyQuote2)
        
        val memberQuote1 = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote1,
                memo = "테스트 메모"
            )
        )

        every { dailyQuoteCacheService.getMonthlyQuotes(any(), any()) } returns emptyList()
        every { dailyQuoteCacheService.cacheMonthlyQuotes(any()) } returns any()
        
        // when
        val result = sut.monthlyQuotes(member, yearMonth)
        
        // then
        assertThat(result.memberQuotes).hasSize(2)
    }
    
    @Test
    fun `회원 명언 목록 조회 성공 - 좋아요한 명언만 조회`() {
        // given
        val member = memberPersistFactory.createMember()
        val pageable = PageRequest.of(0, 10)
        val request = MemberQuotesCommonRequest.fromV1(MemberQuotesRequest(likeYn = "Y"))
        
        val (savedQuote1, savedDailyQuote1) = quotePersistFactory.createQuoteWithDailyQuote()
        val (savedQuote2, savedDailyQuote2) = quotePersistFactory.createQuoteWithDailyQuote()
        
        val memberQuote1 = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote1,
                likeYn = "Y",
                memo = "좋아요한 명언"
            )
        )
        val memberQuote2 = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote2,
                likeYn = "N",
                typingKorQuote = "타이핑 명언"
            )
        )

        every { dailyQuoteCacheService.getMonthlyQuotes(any(), any()) } returns emptyList()
        every { dailyQuoteCacheService.cacheMonthlyQuotes(any()) } returns any()
        
        // when
        val result = sut.memberQuotes(member, pageable, request)
        
        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].memberQuoteSeq).isEqualTo(memberQuote1.memberQuoteSeq)
    }
    
    @Test
    fun `회원 명언 목록 조회 성공 - 내용이 있는 모든 명언 조회`() {
        // given
        val member = memberPersistFactory.createMember()
        val pageable = PageRequest.of(0, 10)
        val request = MemberQuotesCommonRequest.fromV1(MemberQuotesRequest(likeYn = "N"))
        
        val (savedQuote1, savedDailyQuote1) = quotePersistFactory.createQuoteWithDailyQuote()
        val (savedQuote2, savedDailyQuote2) = quotePersistFactory.createQuoteWithDailyQuote()
        val (savedQuote3, savedDailyQuote3) = quotePersistFactory.createQuoteWithDailyQuote()
        
        val memberQuote1 = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote1,
                likeYn = "Y",
                memo = "좋아요한 명언"
            )
        )
        val memberQuote2 = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote2,
                likeYn = "N",
                typingKorQuote = "타이핑 명언"
            )
        )
        val memberQuote3 = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote3,
                likeYn = "N"
            )
        )

        every { dailyQuoteCacheService.getMonthlyQuotes(any(), any()) } returns emptyList()
        every { dailyQuoteCacheService.cacheMonthlyQuotes(any()) } returns any()
        
        // when
        val result = sut.memberQuotes(member, pageable, request)
        
        // then
        assertThat(result.content).hasSize(2)
    }
    
    @Test
    fun `타이핑 명언 조회 성공 - 회원 명언이 있는 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val quote = QuoteEntityFactory.quote(
            korQuote = "한국어 명언",
            korAuthor = "한국 작가"
        )
        val dailyQuote = QuoteEntityFactory.dailyQuote(quote = quote)
        val (savedQuote, savedDailyQuote) = quotePersistFactory.createQuoteWithDailyQuote(quote, dailyQuote)
        val memberQuote = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote,
                typingKorQuote = "한국어 타이핑",
                typingEngQuote = "English Typing"
            )
        )
        
        // when
        val result = sut.typingQuote(member, savedDailyQuote.dailyQuoteSeq!!)
        
        // then
        assertThat(result.korQuote).isEqualTo("한국어 명언")
    }
    
    @Test
    fun `타이핑 명언 조회 실패 - 존재하지 않는 dailyQuoteSeq인 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val dailyQuoteSeq = 999L
        
        // when & then
        assertThatThrownBy { sut.typingQuote(member, dailyQuoteSeq) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
    
    @Test
    fun `회원 명언 조회 by dailyQuoteSeq 성공`() {
        // given
        val member = memberPersistFactory.createMember()
        val (savedQuote, savedDailyQuote) = quotePersistFactory.createQuoteWithDailyQuote()
        val memberQuote = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote
            )
        )
        
        // when
        val result = sut.getMemberQuoteByDailyQuoteSeq(member, savedDailyQuote.dailyQuoteSeq!!)
        
        // then
        assertThat(result).isEqualTo(memberQuote)
    }
    
    @Test
    fun `회원 명언 조회 by memberQuoteSeq 성공`() {
        // given
        val member = memberPersistFactory.createMember()
        val (savedQuote, savedDailyQuote) = quotePersistFactory.createQuoteWithDailyQuote()
        val memberQuote = quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote
            )
        )
        
        // when
        val result = sut.getMemberQuoteByMemberQuoteSeq(member, memberQuote.memberQuoteSeq!!)
        
        // then
        assertThat(result).isEqualTo(memberQuote)
    }
} 