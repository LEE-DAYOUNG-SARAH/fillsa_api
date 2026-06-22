package com.fillsa.app.api.members.quote

import com.fillsa.service.member.MemberQuote

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