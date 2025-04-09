package com.fillsa.fillsa_api.domain.quote.service

import com.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import com.fillsa.fillsa_api.domain.quote.dto.MonthlyQuoteResponse
import com.fillsa.fillsa_api.domain.quote.service.useCase.QuoteUseCase
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class QuoteService(): QuoteUseCase {
    override fun getDailyQuote(quoteDate: LocalDate): DailyQuoteResponse {
        return DailyQuoteResponse(
            dailyQuoteSeq = 1,
            userQuoteSeq = 1,
            korQuote = "나는 죽음을 두려워하지 않는다. 단지 그것이 일어날 때 거기에 있고 싶지 않을 뿐이다.",
            engQuote = "I'm not afraid of death; I just don't want to be there when it happens.",
            typingKorQuote = "나는 죽",
            typingEngQuote = "I'm not a",
            imagePath = null,
            korAuthor = "우디 앨런",
            engAuthor = "Woody Allen",
            authorUrl = "https://ko.wikipedia.org/wiki/우디 앨런"
        )
    }

    override fun getMonthlyQuotes(yearMonth: YearMonth): MonthlyQuoteResponse {
        return MonthlyQuoteResponse(
            memerQuotes = listOf(
                MonthlyQuoteResponse.MemberQuoteData(
                    dailyQuoteSeq = 1,
                    quoteDate = LocalDate.of(2025, 4, 1),
                    korAuthor = "나는 죽음을 두려워하지 않는다. 단지 그것이 일어날 때 거기에 있고 싶지 않을 뿐이다.",
                    typingYn = "Y",
                    likeYn = "Y"
                ),
                MonthlyQuoteResponse.MemberQuoteData(
                    dailyQuoteSeq = 2,
                    quoteDate = LocalDate.of(2025, 4, 3),
                    korAuthor = "사랑은 자연사하지 않는다. 우리는 그것의 원천을 보충하는 방법을 알지 못하기 때문에 죽는다. 그것은 눈먼 실수와 배신으로 인해 죽는다. 그것은 질병과 상처로 인해 죽고, 지침과 시들음과 변색으로 인해 죽는다.",
                    typingYn = "N",
                    likeYn = "Y"
                ),
                MonthlyQuoteResponse.MemberQuoteData(
                    dailyQuoteSeq = 3,
                    quoteDate = LocalDate.of(2025, 4, 5),
                    korAuthor = "죽음에 대한 두려움은 삶에 대한 두려움에서 비롯된다. 온전히 삶을 사는 사람은 언제든 죽을 준비가 되어 있다.",
                    typingYn = "Y",
                    likeYn = "N"
                ),
            ),
            monthlySummary = MonthlyQuoteResponse.MonthlySummaryData(
                typingCount = 2,
                likeCount = 3
            )
        )
    }
}