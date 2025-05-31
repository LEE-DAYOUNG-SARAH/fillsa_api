package store.fillsa.fillsa_api.domain.members.member.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice
import store.fillsa.fillsa_api.domain.members.member.repository.MemberDeviceRepository

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