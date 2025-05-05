package store.fillsa.fillsa_api.domain.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.dto.LikeRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest

data class LoginRequest(
    @Schema(description = "로그인 데이터")
    val loginData: LoginData,

    @Schema(description = "연동 데이터")
    val syncData: List<SyncData> = listOf()
) {
    data class LoginData(
        val tempToken: String,
        val deviceId: String
    )

    data class SyncData(
        val dailyQuoteSeq: Long,
        val typingQuoteRequest: TypingQuoteRequest,
        val memoRequest: MemoRequest,
        val likeRequest: LikeRequest
    )
}

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val memberSeq: Long,
    val nickname: String,
    val profileImageUrl: String?
)