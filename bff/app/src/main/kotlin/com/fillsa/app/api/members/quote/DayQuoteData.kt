package com.fillsa.app.api.members.quote

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.quote.DailyQuote
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 하루치 명언 카드 데이터.
 *
 * 주간 조회(`weekly`)의 days[] 원소와 단일 날짜 조회(`daily`)의 응답이 동일한 형태를 쓴다.
 * 클라이언트가 같은 모델·같은 파서를 재사용하도록 하기 위함이다.
 *
 * 미래 날짜는 문장·질문을 내려주지 않는다 (docs/home-renewal-api-plan.md §5-1).
 */
data class DayQuoteData(
    @Schema(description = "명언 일자", required = true, example = "2026-09-03")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    val date: LocalDate,

    @Schema(description = "요일", required = true, example = "THURSDAY")
    val dayOfWeek: DayOfWeek,

    @Schema(
        description = "날짜 상태. 오늘이면서 필사 완료면 done 을 우선한다",
        required = true,
        allowableValues = ["none", "today", "done"],
        example = "done"
    )
    val state: String,

    @Schema(description = "일별 명언 일련번호. 미래 날짜이거나 배정된 명언이 없으면 null")
    val dailyQuoteSeq: Long?,

    @Schema(description = "한글 명언. 미래 날짜면 null")
    val korQuote: String?,

    @Schema(description = "영문 명언. 미래 날짜면 null")
    val engQuote: String?,

    @Schema(description = "한글 저자. 미래 날짜면 null")
    val korAuthor: String?,

    @Schema(description = "영문 저자. 미래 날짜면 null")
    val engAuthor: String?,

    @Schema(description = "저자 위키백과 url. 미래 날짜면 null")
    val authorUrl: String?,

    @Schema(description = "한글 회고 질문. 미래 날짜면 null")
    val questionKo: String?,

    @Schema(description = "영문 회고 질문. 미래 날짜면 null")
    val questionEn: String?,

    @Schema(description = "오늘의 질문 답변. 미작성이면 null")
    val answer: String?,

    @Schema(description = "답변 최종 수정 시각")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val answeredAt: LocalDateTime?,

    @Schema(description = "좋아요 여부(Y/N)", required = true, example = "N")
    val likeYn: String,

    @Schema(description = "등록한 이미지 url. 없으면 null")
    val imagePath: String?,

    @Schema(description = "필사 완료 여부", required = true)
    val completed: Boolean
) {
    companion object {
        const val STATE_NONE = "none"
        const val STATE_TODAY = "today"
        const val STATE_DONE = "done"

        /** 미래 날짜 — 칸은 그리되 콘텐츠는 공개하지 않는다. */
        fun future(date: LocalDate) = DayQuoteData(
            date = date,
            dayOfWeek = date.dayOfWeek,
            state = STATE_NONE,
            dailyQuoteSeq = null,
            korQuote = null,
            engQuote = null,
            korAuthor = null,
            engAuthor = null,
            authorUrl = null,
            questionKo = null,
            questionEn = null,
            answer = null,
            answeredAt = null,
            likeYn = "N",
            imagePath = null,
            completed = false
        )

        /** 명언이 배정되지 않은 과거 날짜 — 사용자 기록도 있을 수 없다. */
        fun empty(date: LocalDate, today: LocalDate) = DayQuoteData(
            date = date,
            dayOfWeek = date.dayOfWeek,
            state = if (date.isEqual(today)) STATE_TODAY else STATE_NONE,
            dailyQuoteSeq = null,
            korQuote = null,
            engQuote = null,
            korAuthor = null,
            engAuthor = null,
            authorUrl = null,
            questionKo = null,
            questionEn = null,
            answer = null,
            answeredAt = null,
            likeYn = "N",
            imagePath = null,
            completed = false
        )

        fun from(
            koAuthorUrl: String,
            enAuthorUrl: String,
            dailyQuote: DailyQuote,
            memberQuote: MemberQuote?,
            today: LocalDate
        ): DayQuoteData {
            val date = dailyQuote.quoteDate
            val completed = memberQuote?.completed ?: false

            return DayQuoteData(
                date = date,
                dayOfWeek = date.dayOfWeek,
                state = resolveState(date, today, completed),
                dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
                korQuote = dailyQuote.quote.korQuote,
                engQuote = dailyQuote.quote.engQuote,
                korAuthor = dailyQuote.quote.korAuthor,
                engAuthor = dailyQuote.quote.engAuthor,
                authorUrl = authorUrlOf(koAuthorUrl, enAuthorUrl, dailyQuote),
                questionKo = dailyQuote.quote.questionKo,
                questionEn = dailyQuote.quote.questionEn,
                answer = memberQuote?.answer,
                answeredAt = memberQuote?.answeredAt,
                likeYn = memberQuote?.likeYn ?: "N",
                imagePath = memberQuote?.imagePath,
                completed = completed
            )
        }

        /** 오늘이면서 완료면 done 우선 (PRD 명시) */
        private fun resolveState(date: LocalDate, today: LocalDate, completed: Boolean) = when {
            completed -> STATE_DONE
            date.isEqual(today) -> STATE_TODAY
            else -> STATE_NONE
        }

        private fun authorUrlOf(koAuthorUrl: String, enAuthorUrl: String, dailyQuote: DailyQuote): String? =
            dailyQuote.quote.korAuthor?.let { "$koAuthorUrl$it" }
                ?: dailyQuote.quote.engAuthor?.let { "$enAuthorUrl$it" }
    }
}
