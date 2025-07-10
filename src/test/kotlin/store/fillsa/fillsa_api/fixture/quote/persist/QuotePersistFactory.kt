package store.fillsa.fillsa_api.fixture.quote.persist

import org.springframework.stereotype.Component
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import store.fillsa.fillsa_api.domain.quote.entity.Quote
import store.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository
import store.fillsa.fillsa_api.domain.quote.repository.QuoteRepository
import store.fillsa.fillsa_api.fixture.quote.entity.QuoteEntityFactory

@Component
class QuotePersistFactory(
    private val quoteRepository: QuoteRepository,
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val memberQuoteRepository: MemberQuoteRepository,
) {
    fun createQuote(quote: Quote = QuoteEntityFactory.quote()): Quote {
        return quoteRepository.save(quote)
    }

    fun createDailyQuote(dailyQuote: DailyQuote = QuoteEntityFactory.dailyQuote()): DailyQuote {
        return dailyQuoteRepository.save(dailyQuote)
    }

    fun createMemberQuote(memberQuote: MemberQuote = QuoteEntityFactory.memberQuote()): MemberQuote {
        return memberQuoteRepository.save(memberQuote)
    }
    
    // 편의 메서드들
    fun createQuoteWithDailyQuote(
        quote: Quote = QuoteEntityFactory.quote(),
        dailyQuote: DailyQuote? = null
    ): Pair<Quote, DailyQuote> {
        val savedQuote = createQuote(quote)
        val savedDailyQuote = createDailyQuote(
            dailyQuote ?: QuoteEntityFactory.dailyQuote(quote = savedQuote)
        )
        return savedQuote to savedDailyQuote
    }
    
    fun createCompleteQuoteSet(
        member: Member,
        quote: Quote = QuoteEntityFactory.quote(),
        dailyQuote: DailyQuote? = null,
        memberQuote: MemberQuote? = null
    ): Triple<Quote, DailyQuote, MemberQuote> {
        val (savedQuote, savedDailyQuote) = createQuoteWithDailyQuote(quote, dailyQuote)
        val savedMemberQuote = createMemberQuote(
            memberQuote ?: QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = savedDailyQuote
            )
        )
        return Triple(savedQuote, savedDailyQuote, savedMemberQuote)
    }
} 