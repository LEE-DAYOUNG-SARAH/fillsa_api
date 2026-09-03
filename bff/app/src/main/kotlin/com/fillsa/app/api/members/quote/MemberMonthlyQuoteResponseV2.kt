package com.fillsa.app.api.members.quote

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.quote.DailyQuote
import java.time.LocalDate
import java.time.LocalDateTime

class MemberMonthlyQuoteResponseV2 (
    @Schema(description = "사용자 명언 정보", required = true)
    val memberQuotes: List<MemberQuotesDataV2>,

    @Schema(description = "월별 요약 정보", required = true)
    val monthlySummary: MonthlySummaryDataV2
) {
        data class MemberQuotesDataV2(
            @Schema(description = "일별 명언 일련번호", required = true)
            val dailyQuoteSeq: Long,

            @Schema(description = "명언 일자", required = true)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
            val quoteDate: LocalDate,

            @Schema(description = "명언", required = true)
            val quote: String,

            @Schema(description = "명언 저자", required = true)
            val author: String,

            @Schema(description = "필사 완료 여부", readOnly = true)
            val completed: Boolean,

            @Schema(description = "연속 필사 여부", required = true)
            val todayCompleted: Boolean,

            @Schema(description = "좋아요 여부", example = "Y/N", required = true)
            val likeYn: String,

            // ↓ 캘린더 날짜 상세를 월간 응답에 통합하며 추가 (docs/home-renewal-api-plan.md §4)

            @Schema(description = "영문 명언")
            val engQuote: String?,

            @Schema(description = "영문 저자")
            val engAuthor: String?,

            @Schema(description = "저자 위키백과 url")
            val authorUrl: String?,

            @Schema(description = "한글 회고 질문")
            val questionKo: String?,

            @Schema(description = "영문 회고 질문")
            val questionEn: String?,

            @Schema(description = "오늘의 질문 답변. 미작성이면 null")
            val answer: String?,

            @Schema(description = "답변 최종 수정 시각")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            val answeredAt: LocalDateTime?,

            @Schema(description = "등록한 이미지 url. 없으면 null")
            val imagePath: String?
        )

        data class MonthlySummaryDataV2(
            @Schema(description = "타이핑 갯수", required = true)
            val typingCount: Int,

            @Schema(description = "좋아요 갯수", required = true)
            val likeCount: Int,

            @Schema(description = "연속필사 갯수", required = true)
            val streakCount: Int
        )

        companion object {
            fun from(
                koAuthorUrl: String,
                enAuthorUrl: String,
                quotes: List<DailyQuote>,
                memberQuotes: List<MemberQuote>
            ): MemberMonthlyQuoteResponseV2 {
                val memberQuoteData = quotes.map { dailyQuote ->
                    val memberQuote = memberQuotes.find { it.dailyQuote.dailyQuoteSeq == dailyQuote.dailyQuoteSeq }
                    MemberQuotesDataV2(
                        dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
                        quoteDate = dailyQuote.quoteDate,
                        quote = dailyQuote.quote.korQuote ?: dailyQuote.quote.engQuote.orEmpty(),
                        author = dailyQuote.quote.korAuthor ?: dailyQuote.quote.engAuthor.orEmpty(),
                        completed = memberQuote?.completed ?: false,
                        todayCompleted = memberQuote?.todayCompleted ?: false,
                        likeYn = memberQuote?.likeYn ?: "N",
                        engQuote = dailyQuote.quote.engQuote,
                        engAuthor = dailyQuote.quote.engAuthor,
                        authorUrl = dailyQuote.quote.korAuthor?.let { "$koAuthorUrl$it" }
                            ?: dailyQuote.quote.engAuthor?.let { "$enAuthorUrl$it" },
                        questionKo = dailyQuote.quote.questionKo,
                        questionEn = dailyQuote.quote.questionEn,
                        answer = memberQuote?.answer,
                        answeredAt = memberQuote?.answeredAt,
                        imagePath = memberQuote?.imagePath
                    )
                }

                return MemberMonthlyQuoteResponseV2(
                    memberQuotes = memberQuoteData,
                    monthlySummary = MonthlySummaryDataV2(
                        typingCount = memberQuoteData.count { it.completed },
                        likeCount = memberQuoteData.count { it.likeYn == "Y" },
                        streakCount = memberQuoteData.count { it.todayCompleted }
                    )
                )
            }
        }
    }