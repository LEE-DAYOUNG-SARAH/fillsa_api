package store.fillsa.fillsa_api.domain.members.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice

interface MemberDeviceRepository: JpaRepository<MemberDevice, Long> {
    fun findByMemberAndDeviceId(member: Member, deviceId: String): MemberDevice?
}