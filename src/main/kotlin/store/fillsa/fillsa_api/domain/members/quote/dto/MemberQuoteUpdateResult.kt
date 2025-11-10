package store.fillsa.fillsa_api.domain.members.quote.dto

import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote

data class MemberQuoteUpdateResult(
    val memberQuote: MemberQuote,
    val completedChanged: Boolean,
    val todayCompletedChanged: Boolean
) {
    companion object {
        fun of(
            memberQuote: MemberQuote,
            wasCompleted: Boolean,
            wasTodayCompleted: Boolean
        ): MemberQuoteUpdateResult {
            return MemberQuoteUpdateResult(
                memberQuote = memberQuote,
                completedChanged = !wasCompleted && memberQuote.completed,
                todayCompletedChanged = !wasTodayCompleted && memberQuote.todayCompleted
            )
        }
    }
}