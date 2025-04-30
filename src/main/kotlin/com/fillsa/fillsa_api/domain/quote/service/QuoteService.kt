package com.fillsa.fillsa_api.domain.quote.service

import com.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import com.fillsa.fillsa_api.domain.quote.service.useCase.QuoteUseCase
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class QuoteService(): QuoteUseCase {
    override fun getDailyQuote(quoteDate: LocalDate): DailyQuoteResponse {
        return DailyQuoteResponse(
            dailyQuoteSeq = 1,
            korQuote = "나는 죽음을 두려워하지 않는다. 단지 그것이 일어날 때 거기에 있고 싶지 않을 뿐이다.",
            engQuote = "I'm not afraid of death; I just don't want to be there when it happens.",
            korAuthor = "우디 앨런",
            engAuthor = "Woody Allen",
            authorUrl = "https://ko.wikipedia.org/wiki/우디 앨런"
        )
    }
}