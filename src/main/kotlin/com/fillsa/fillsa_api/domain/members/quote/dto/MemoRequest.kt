package com.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema

data class MemoRequest(
    @Schema(description = "메모")
    val memo: String
)