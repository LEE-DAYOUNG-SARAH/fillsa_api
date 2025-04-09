package com.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema

data class TypingQuoteRequest(
    @Schema(description = "언어")
    val language: Language,

    @Schema(description = "타이핑 명언")
    val typingQuote: String?
) {
    enum class Language {
        KOR, ENG
    }
}