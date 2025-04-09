package com.fillsa.fillsa_api.domain.quote.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.multipart.MultipartFile

data class DailyQuoteResponse(
    @Schema(description = "일별 명언 일련번호")
    val dailyQuoteSeq: Long,

    @Schema(description = "사용자 명언 일련번호")
    val userQuoteSeq: Long?,

    @Schema(description = "국문 명언")
    val korQuote: String?,

    @Schema(description = "영문 명언")
    val engQuote: String?,

    @Schema(description = "타이핑 국문 명언")
    val typingKorQuote: String?,

    @Schema(description = "타이핑 영문 명언")
    val typingEngQuote: String?,

    @Schema(description = "s3 이미지 경로")
    val imagePath: String?,

    @Schema(description = "국문 저자")
    val korAuthor: String?,

    @Schema(description = "영문 저자")
    val engAuthor: String?,

    @Schema(description = "저자 url")
    val authorUrl: String?
)
