package com.fillsa.fillsa_api.domain.quote.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.multipart.MultipartFile

open class DailyQuoteResponse(
    @Schema(description = "일별 명언 일련번호")
    val dailyQuoteSeq: Long,

    @Schema(description = "국문 명언")
    val korQuote: String?,

    @Schema(description = "영문 명언")
    val engQuote: String?,

    @Schema(description = "국문 저자")
    val korAuthor: String?,

    @Schema(description = "영문 저자")
    val engAuthor: String?,

    @Schema(description = "저자 url")
    val authorUrl: String?
)