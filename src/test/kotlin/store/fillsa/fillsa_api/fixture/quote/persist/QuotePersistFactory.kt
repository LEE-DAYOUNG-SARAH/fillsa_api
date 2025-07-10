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
import store.fillsa.fillsa_api.fixture.member.persist.MemberPersistFactory

@Component
class QuotePersistFactory(
    private val quoteRepository: QuoteRepository,
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val memberQuoteRepository: MemberQuoteRepository,
    private val memberPersistFactory: MemberPersistFactory,
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
        val savedDailyQuote = if (dailyQuote != null) {
            // dailyQuote가 전달된 경우, 저장된 quote로 교체하여 저장
            val updatedDailyQuote = QuoteEntityFactory.dailyQuote(
                quote = savedQuote,
                quoteDate = dailyQuote.quoteDate,
                quoteDayOfWeek = dailyQuote.quoteDayOfWeek
            )
            createDailyQuote(updatedDailyQuote)
        } else {
            // dailyQuote가 null인 경우 기본값으로 생성
            createDailyQuote(QuoteEntityFactory.dailyQuote(quote = savedQuote))
        }
        return savedQuote to savedDailyQuote
    }

    fun createCompleteQuoteSet(
        quote: Quote = QuoteEntityFactory.quote(),
        dailyQuote: DailyQuote? = null,
        memberQuote: MemberQuote? = null
    ): Triple<Quote, DailyQuote, MemberQuote> {
        // 1. Member 저장
        val savedMember = memberPersistFactory.createMember()
        
        // 2. Quote, DailyQuote 저장
        val (savedQuote, savedDailyQuote) = createQuoteWithDailyQuote(quote, dailyQuote)
        
        // 3. MemberQuote 저장 (저장된 엔티티들 사용)
        val savedMemberQuote = if (memberQuote != null) {
            // memberQuote가 전달된 경우, 저장된 엔티티들로 교체하여 생성
            val updatedMemberQuote = QuoteEntityFactory.memberQuote(
                member = savedMember,
                dailyQuote = savedDailyQuote,
                typingKorQuote = memberQuote.typingKorQuote,
                typingEngQuote = memberQuote.typingEngQuote,
                imagePath = memberQuote.imagePath,
                memo = memberQuote.memo,
                likeYn = memberQuote.likeYn
            )
            createMemberQuote(updatedMemberQuote)
        } else {
            // memberQuote가 null인 경우 기본값으로 생성
            createMemberQuote(
                QuoteEntityFactory.memberQuote(
                    member = savedMember,
                    dailyQuote = savedDailyQuote
                )
            )
        }
        
        return Triple(savedQuote, savedDailyQuote, savedMemberQuote)
    }
} 