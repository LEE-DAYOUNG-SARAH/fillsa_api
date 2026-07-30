package com.fillsa.app.api.members.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.fillsa.app.common.exception.ApiErrorResponses
import com.fillsa.app.service.members.member.MemberDeviceService
import com.fillsa.app.service.push.PushTestService
import com.fillsa.service.member.Member
import com.fillsa.util.exception.ErrorCode.INVALID_REQUEST
import com.fillsa.util.exception.ErrorCode.NOT_FOUND

@RestController
@RequestMapping("/api/v1/member-devices")
@Tag(name = "디바이스")
class MemberDeviceController(
    private val memberDeviceService: MemberDeviceService,
    private val pushTestService: PushTestService,
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

    @ApiErrorResponses(NOT_FOUND, INVALID_REQUEST)
    @PostMapping("/push/test")
    @Operation(summary = "푸시 테스트 발송 api", description = "본인 디바이스에 즉시 1건 발송 (09시 스케줄러와 동일 경로 — 개발/QA 검증용)")
    fun sendTestPush(
        @AuthenticationPrincipal member: Member,
        @RequestBody request: PushTestRequest,
    ): PushTestResponse = PushTestResponse(
        sent = pushTestService.sendTestPush(member, request.deviceId)
    )
}

data class PushTestRequest(
    @Schema(description = "디바이스 Id", required = true)
    val deviceId: String,
)

data class PushTestResponse(
    @Schema(description = "발송 성공 여부", required = true)
    val sent: Boolean,
)

data class PushUpdateRequest(
    @Schema(description = "디바이스 Id", required = true)
    val deviceId: String,

    @Schema(description = "FCM 푸시 토큰 (거부 시 null 허용)")
    val pushToken: String? = null,

    @Schema(description = "푸시 수신 동의 여부", required = true)
    val agreed: Boolean,
)
