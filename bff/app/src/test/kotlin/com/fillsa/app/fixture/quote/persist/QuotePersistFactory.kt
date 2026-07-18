package com.fillsa.app.fixture.quote.persist

import org.springframework.stereotype.Component
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.member.MemberStreak
import com.fillsa.service.member.MemberQuoteRepository
import com.fillsa.service.member.MemberStreakRepository
import com.fillsa.service.quote.DailyQuote
import com.fillsa.service.quote.Quote
import com.fillsa.service.quote.DailyQuoteRepository
import com.fillsa.app.api.quote.repository.QuoteRepository
import com.fillsa.app.fixture.quote.entity.QuoteEntityFactory
import com.fillsa.app.fixture.member.persist.MemberPersistFactory

@Component
class QuotePersistFactory(
    private val quoteRepository: QuoteRepository,
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val memberQuoteRepository: MemberQuoteRepository,
    private val memberPersistFactory: MemberPersistFactory,
    private val memberStreakRepository: MemberStreakRepository
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

    fun createMemberStreak(memberStreak: MemberStreak = QuoteEntityFactory.memberStreak()): MemberStreak {
        return memberStreakRepository.save(memberStreak)
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

        // 2. MemberStreak 저장
        createMemberStreak(QuoteEntityFactory.memberStreak(member = savedMember))
        
        // 3. Quote, DailyQuote 저장
        val (savedQuote, savedDailyQuote) = createQuoteWithDailyQuote(quote, dailyQuote)
        
        // 4. MemberQuote 저장 (저장된 엔티티들 사용)
        val savedMemberQuote = if (memberQuote != null) {
            // memberQuote가 전달된 경우, 저장된 엔티티들로 교체하여 생성
            val updatedMemberQuote = QuoteEntityFactory.memberQuote(
                member = savedMember,
                dailyQuote = savedDailyQuote,
                typingKorQuote = memberQuote.typingKorQuote,
                typingEngQuote = memberQuote.typingEngQuote,
                imagePath = memberQuote.imagePath,
                memo = memberQuote.memo,
                likeYn = memberQuote.likeYn,
                completed = memberQuote.completed,
                todayCompleted = memberQuote.todayCompleted
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