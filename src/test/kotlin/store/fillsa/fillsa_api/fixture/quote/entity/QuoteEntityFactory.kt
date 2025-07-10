package store.fillsa.fillsa_api.fixture.quote.entity

import store.fillsa.fillsa_api.domain.quote.entity.Quote
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.fixture.member.entity.MemberEntityFactory
import java.time.LocalDate

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
        ) = MemberQuote(
            member = member,
            dailyQuote = dailyQuote,
            typingKorQuote = typingKorQuote,
            typingEngQuote = typingEngQuote,
            imagePath = imagePath,
            memo = memo,
            likeYn = likeYn,
        )
    }
} 