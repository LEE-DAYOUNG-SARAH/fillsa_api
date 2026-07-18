package com.fillsa.admin.service.member

import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDevice
import java.time.LocalDateTime

data class MemberListItem(
    val memberSeq: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    val oauthProvider: Member.OAuthProvider,
    val adminYn: String,
    val withdrawalYn: String,
    val currentStreak: Int,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(member: Member, currentStreak: Int) = MemberListItem(
            memberSeq = member.memberSeq,
            nickname = member.nickname,
            profileImageUrl = member.profileImageUrl,
            oauthProvider = member.oauthProvider,
            adminYn = member.adminYn,
            withdrawalYn = member.withdrawalYn,
            currentStreak = currentStreak,
            createdAt = member.createdAt,
        )
    }
}

data class MemberDeviceResponse(
    val memberDeviceSeq: Long,
    val deviceModel: String?,
    val osType: MemberDevice.OsType,
    val osVersion: String?,
    val appVersion: String?,
    val activeYn: String,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(device: MemberDevice) = MemberDeviceResponse(
            memberDeviceSeq = device.memberDeviceSeq,
            deviceModel = device.deviceModel,
            osType = device.osType,
            osVersion = device.osVersion,
            appVersion = device.appVersion,
            activeYn = device.activeYn,
            updatedAt = device.updatedAt,
        )
    }
}

data class MemberDetailResponse(
    val memberSeq: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    val oauthProvider: Member.OAuthProvider,
    val adminYn: String,
    val withdrawalYn: String,
    val currentStreak: Int,
    val createdAt: LocalDateTime,
    val oauthId: String,
    val withdrawalAt: LocalDateTime?,
    val maxStreak: Int,
    val totalCompletedCount: Int,
    val devices: List<MemberDeviceResponse>,
) {
    companion object {
        fun from(
            member: Member,
            currentStreak: Int,
            maxStreak: Int,
            totalCompletedCount: Int,
            devices: List<MemberDeviceResponse>,
        ) = MemberDetailResponse(
            memberSeq = member.memberSeq,
            nickname = member.nickname,
            profileImageUrl = member.profileImageUrl,
            oauthProvider = member.oauthProvider,
            adminYn = member.adminYn,
            withdrawalYn = member.withdrawalYn,
            currentStreak = currentStreak,
            createdAt = member.createdAt,
            oauthId = member.oauthId,
            withdrawalAt = member.withdrawalAt,
            maxStreak = maxStreak,
            totalCompletedCount = totalCompletedCount,
            devices = devices,
        )
    }
}

data class MemberSummaryResponse(
    val totalCount: Long,
    val activeCount: Long,
    val withdrawnCount: Long,
    val adminCount: Long,
)

/** 팀원(관리자) 표시 지정/해제 요청. adminYn 은 YnFlag("Y"/"N"). */
data class MemberAdminRoleRequest(
    val adminYn: String,
)
