package com.fillsa.app.fixture.quote.entity

import com.fillsa.service.quote.Quote
import com.fillsa.service.quote.DailyQuote
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberStreak
import com.fillsa.app.fixture.member.entity.MemberEntityFactory
import java.time.LocalDate
import java.time.LocalDateTime

class QuoteEntityFactory {
    companion object {
        fun quote(
            korQuote: String? = "한국어 명언입니다.",
            engQuote: String? = "This is an English quote.",
            korAuthor: String? = "한국 작가",
            engAuthor: String? = "English Author",
            category: String? = "카테고리",
        ) = Quote(
            korQuote = korQuote,
            engQuote = engQuote,
            korAuthor = korAuthor,
            engAuthor = engAuthor,
            category = category,
        )

        fun dailyQuote(
            quote: Quote = quote(),
            quoteDate: LocalDate = LocalDate.now(),
            quoteDayOfWeek: String = "월",
        ) = DailyQuote(
            quote = quote,
            quoteDate = quoteDate,
            quoteDayOfWeek = quoteDayOfWeek,
        )

        fun memberQuote(
            member: Member = MemberEntityFactory.member(),
            dailyQuote: DailyQuote = dailyQuote(),
            typingKorQuote: String? = null,
            typingEngQuote: String? = null,
            imagePath: String? = null,
            memo: String? = null,
            likeYn: String = "N",
            completed: Boolean = false,
            todayCompleted: Boolean = false,
            answer: String? = null,
            answeredAt: LocalDateTime? = null,
        ) = MemberQuote(
            member = member,
            dailyQuote = dailyQuote,
            typingKorQuote = typingKorQuote,
            typingEngQuote = typingEngQuote,
            imagePath = imagePath,
            memo = memo,
            likeYn = likeYn,
            completed = completed,
            todayCompleted = todayCompleted,
            answer = answer,
            answeredAt = answeredAt,
        )

        fun memberStreak(
            member: Member = MemberEntityFactory.member(),
            currentStreak: Int = 0,
            maxStreak: Int = 0,
            lastWrittenDate: LocalDate? = null
        ) = MemberStreak(
            member = member,
            currentStreak = currentStreak,
            maxStreak = maxStreak,
            lastWrittenDate = lastWrittenDate
        )
    }
} 