package store.fillsa.fillsa_api.domain.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.dto.LikeRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest

data class LoginRequest(
    @Schema(description = "로그인 데이터", required = true)
    val loginData: LoginData,

    @Schema(description = "연동 데이터")
    val syncData: List<SyncData> = listOf()
) {
    data class LoginData(
        @Schema(description = "임시토큰", required = true)
        val tempToken: String,
        @Schema(description = "디바이스 Id", required = true)
        val deviceId: String
    )

    data class SyncData(
        @Schema(description = "일별 명언 일련번호", required = true)
        val dailyQuoteSeq: Long,

        @Schema(description = "타이핑 요청")
        val typingQuoteRequest: TypingQuoteRequest?,

        @Schema(description = "메모 요청")
        val memoRequest: MemoRequest?,

        @Schema(description = "좋아요 요청")
        val likeRequest: LikeRequest?
    )
}

data class LoginResponse(
    @Schema(description = "access Token", required = true)
    val accessToken: String,

    @Schema(description = "refresh token", required = true)
    val refreshToken: String,

    @Schema(description = "사용자 일련번호", required = true)
    val memberSeq: Long,

    @Schema(description = "닉네임", required = true)
    val nickname: String,

    @Schema(description = "프로필 이미지 url")
    val profileImageUrl: String?
)