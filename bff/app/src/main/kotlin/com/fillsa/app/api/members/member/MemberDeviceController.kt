package com.fillsa.app.api.members.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.fillsa.app.common.exception.ApiErrorResponses
import com.fillsa.app.service.members.member.MemberDeviceService
import com.fillsa.service.member.Member
import com.fillsa.util.exception.ErrorCode.NOT_FOUND

@RestController
@RequestMapping("/api/v1/member-devices")
@Tag(name = "디바이스")
class MemberDeviceController(
    private val memberDeviceService: MemberDeviceService,
) {
    @ApiErrorResponses(NOT_FOUND)
    @PutMapping("/push")
    @Operation(summary = "푸시 토큰·수신 동의 등록/갱신 api", description = "알림 권한 허용/거부, 설정 토글, FCM 토큰 리프레시 시 호출")
    fun updatePush(
        @AuthenticationPrincipal member: Member,
        @RequestBody request: PushUpdateRequest,
    ) {
        memberDeviceService.updatePush(member, request.deviceId, request.pushToken, request.agreed)
    }
}

data class PushUpdateRequest(
    @Schema(description = "디바이스 Id", required = true)
    val deviceId: String,

    @Schema(description = "FCM 푸시 토큰 (거부 시 null 허용)")
    val pushToken: String? = null,

    @Schema(description = "푸시 수신 동의 여부", required = true)
    val agreed: Boolean,
)
