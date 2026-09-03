package com.fillsa.app.api.members.quote

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import com.fillsa.service.member.MemberQuote
import java.time.LocalDateTime

data class AnswerRequest(
    @Schema(description = "오늘의 질문 답변 (최대 200자, 공백만 입력 불가)", required = true)
    val answer: String
) {
    companion object {
        const val MAX_LENGTH = 200
    }
}

data class AnswerResponse(
    @Schema(description = "사용자 명언 일련번호", required = true)
    val memberQuoteSeq: Long,

    @Schema(description = "저장된 답변", required = true)
    val answer: String,

    @Schema(description = "답변 최종 수정 시각", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val answeredAt: LocalDateTime
) {
    companion object {
        fun from(memberQuote: MemberQuote) = AnswerResponse(
            memberQuoteSeq = memberQuote.memberQuoteSeq,
            answer = memberQuote.answer.orEmpty(),
            answeredAt = memberQuote.answeredAt ?: LocalDateTime.now()
        )
    }
}
