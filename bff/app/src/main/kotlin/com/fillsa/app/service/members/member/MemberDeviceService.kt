package com.fillsa.app.service.members.member

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.api.auth.LoginRequest
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDevice
import com.fillsa.service.member.MemberDeviceRepository

@Service
class MemberDeviceService(
    private val memberDeviceRepository: MemberDeviceRepository
) {
    @Transactional
    fun create(member: Member, request: LoginRequest.DeviceData): MemberDevice {
        val memberDevice = getMemberDevice(member, request.deviceId)

        return if(memberDevice == null) {
            memberDeviceRepository.save(request.toEntity(member))
        } else {
            memberDevice.update(request.appVersion, request.osVersion)
            memberDevice
        }
    }

    private fun getMemberDevice(member: Member, deviceId: String): MemberDevice? {
        return memberDeviceRepository.findByMemberAndDeviceId(member, deviceId)
    }

    @Transactional
    fun logout(member: Member, deviceId: String) {
        getMemberDevice(member, deviceId)?.logout()
    }
}