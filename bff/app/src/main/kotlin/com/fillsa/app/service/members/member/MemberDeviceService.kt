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

    /**
     * 탈퇴 시 해당 회원의 모든 디바이스를 비활성화하고 푸시 토큰을 제거한다.
     *
     * 이 처리가 없으면 탈퇴 후에도 푸시가 계속 발송된다.
     * 발송 대상 조회(findAllByOsTypeAndActiveYnAndPushAgreedYn)가 회원의 탈퇴 여부를 보지 않기 때문이다.
     * 같은 기기로 재가입하면 옛 디바이스 행이 남아 동일 토큰으로 중복 발송되는 문제도 있었다.
     */
    @Transactional
    fun withdrawAllDevices(member: Member) {
        memberDeviceRepository.findAllByMember(member).forEach {
            it.logout()
            it.clearPushToken()
        }
    }
}