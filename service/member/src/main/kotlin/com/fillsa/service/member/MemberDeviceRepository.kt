package com.fillsa.service.member

import org.springframework.data.jpa.repository.JpaRepository
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDevice

interface MemberDeviceRepository: JpaRepository<MemberDevice, Long> {
    fun findByMemberAndDeviceId(member: Member, deviceId: String): MemberDevice?

    /** 푸시 발송 대상 조회 (OS·활성·수신동의 기준. pushToken null 제외는 호출부에서) */
    fun findAllByOsTypeAndActiveYnAndPushAgreedYn(
        osType: MemberDevice.OsType,
        activeYn: String,
        pushAgreedYn: String,
    ): List<MemberDevice>
}