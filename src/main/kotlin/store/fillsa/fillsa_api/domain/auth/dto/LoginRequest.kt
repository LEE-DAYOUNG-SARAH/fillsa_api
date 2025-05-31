package store.fillsa.fillsa_api.domain.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.common.security.TokenInfo
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice
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
        @Schema(description = "사용자 정보", required = true)
        val userData: UserData,

        @Schema(description = "디바이스 정보", required = true)
        val deviceData: DeviceData
    )

    data class UserData(
        @Schema(description = "oauth provider", required = true)
        val oAuthProvider: Member.OAuthProvider,

        @Schema(description = "oAuth id", required = true)
        val oAuthId: String,

        @Schema(description = "닉네임", required = true)
        val nickname: String,

        @Schema(description = "프로필 이미지 url")
        val profileImageUrl: String?
    ) {
        fun toEntity() = Member(
            oauthId = oAuthId,
            oauthProvider = oAuthProvider,
            nickname = nickname,
            profileImageUrl = profileImageUrl
        )
    }

    data class DeviceData(
        @Schema(description = "디바이스 Id", required = true)
        val deviceId: String,

        @Schema(description = "os type", required = true)
        val osType: MemberDevice.OsType,

        @Schema(description = "app version", required = true)
        val appVersion: String,

        @Schema(description = "os version", example = "Android 14", required = true)
        val osVersion: String,

        @Schema(description = "디바이스 모델", example = "Galaxy S24", required = true)
        val deviceModel: String
    ) {
        fun toEntity(member: Member) = MemberDevice(
            member = member,
            deviceId = deviceId,
            osType = osType,
            appVersion = appVersion,
            osVersion = osVersion,
            deviceModel = deviceModel,
        )
    }

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
) {
    companion object {
        fun from(token: TokenInfo, member: Member) = LoginResponse(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            memberSeq = member.memberSeq,
            nickname = member.nickname.orEmpty(),
            profileImageUrl = member.profileImageUrl
        )
    }
}