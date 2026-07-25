package com.fillsa.app.service.members.member

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.api.auth.LoginRequest
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDevice
import com.fillsa.service.member.MemberDeviceRepository
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode

@Service
class MemberDeviceService(
    private val memberDeviceRepository: MemberDeviceRepository
) {
    @Transactional
    fun create(member: Member, request: LoginRequest.DeviceData): MemberDevice {
        val memberDevice = getMemberDevice(member, request.deviceId)

        val device = if(memberDevice == null) {
            memberDeviceRepository.save(request.toEntity(member))
        } else {
            memberDevice.update(request.appVersion, request.osVersion)
            memberDevice
        }

        // 로그인 요청에 푸시 정보가 실려 오면 함께 갱신 (optional — 구버전 앱 하위호환)
        if (request.pushToken != null || request.pushAgreed != null) {
            device.updatePush(
                pushToken = request.pushToken ?: device.pushToken,
                agreed = request.pushAgreed ?: (device.pushAgreedYn == "Y"),
            )
        }
        return device
    }

    /** 푸시 토큰·수신 동의 갱신 (권한 허용/거부, 설정 토글, FCM 토큰 리프레시) */
    @Transactional
    fun updatePush(member: Member, deviceId: String, pushToken: String?, agreed: Boolean) {
        val device = getMemberDevice(member, deviceId)
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "등록되지 않은 디바이스: $deviceId")
        device.updatePush(pushToken, agreed)
    }

    private fun getMemberDevice(member: Member, deviceId: String): MemberDevice? {
        return memberDeviceRepository.findByMemberAndDeviceId(member, deviceId)
    }

    @Transactional
    fun logout(member: Member, deviceId: String) {
        getMemberDevice(member, deviceId)?.logout()
    }
}