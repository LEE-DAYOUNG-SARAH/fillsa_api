package com.fillsa.service.member

import org.springframework.data.jpa.repository.JpaRepository
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDevice

interface MemberDeviceRepository: JpaRepository<MemberDevice, Long> {
    fun findByMemberAndDeviceId(member: Member, deviceId: String): MemberDevice?
}