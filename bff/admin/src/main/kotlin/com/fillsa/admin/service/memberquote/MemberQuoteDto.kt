package com.fillsa.admin.service.memberquote

import com.fillsa.admin.service.quote.QuoteResponse
import com.fillsa.service.member.MemberQuote
import java.time.LocalDate

data class MemberQuoteListItem(
    val memberQuoteSeq: Long,
    val memberSeq: Long,
    val nickname: String?,
    val quoteDate: LocalDate,
    val korQuote: String?,
    val typingYn: String,
    val imageYn: String,
    val likeYn: String,
    val completed: Boolean,
    val todayCompleted: Boolean,
) {
    companion object {
        fun from(memberQuote: MemberQuote): MemberQuoteListItem {
            val dailyQuote = memberQuote.dailyQuote
            return MemberQuoteListItem(
                memberQuoteSeq = memberQuote.memberQuoteSeq,
                memberSeq = memberQuote.member.memberSeq,
                nickname = memberQuote.member.nickname,
                quoteDate = dailyQuote.quoteDate,
                korQuote = dailyQuote.quote.korQuote,
                typingYn = memberQuote.getTypingYn(),
                imageYn = if (!memberQuote.imagePath.isNullOrEmpty()) "Y" else "N",
                likeYn = memberQuote.likeYn,
                completed = memberQuote.completed,
                todayCompleted = memberQuote.todayCompleted,
            )
        }
    }
}

data class MemberQuoteDetailResponse(
    val memberQuoteSeq: Long,
    val memberSeq: Long,
    val nickname: String?,
    val quoteDate: LocalDate,
    val korQuote: String?,
    val typingYn: String,
    val imageYn: String,
    val likeYn: String,
    val completed: Boolean,
    val todayCompleted: Boolean,
    val typingKorQuote: String?,
    val typingEngQuote: String?,
    val memo: String?,
    val imagePath: String?,
    val quote: QuoteResponse,
) {
    companion object {
        fun from(memberQuote: MemberQuote, assignedCount: Int): MemberQuoteDetailResponse {
            val listItem = MemberQuoteListItem.from(memberQuote)
            return MemberQuoteDetailResponse(
                memberQuoteSeq = listItem.memberQuoteSeq,
                memberSeq = listItem.memberSeq,
                nickname = listItem.nickname,
                quoteDate = listItem.quoteDate,
                korQuote = listItem.korQuote,
                typingYn = listItem.typingYn,
                imageYn = listItem.imageYn,
                likeYn = listItem.likeYn,
                completed = listItem.completed,
                todayCompleted = listItem.todayCompleted,
                typingKorQuote = memberQuote.typingKorQuote,
                typingEngQuote = memberQuote.typingEngQuote,
                memo = memberQuote.memo,
                imagePath = memberQuote.imagePath,
                quote = QuoteResponse.from(memberQuote.dailyQuote.quote, assignedCount),
            )
        }
    }
}
