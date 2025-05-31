package store.fillsa.fillsa_api.fixture.member.entity

import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice
import java.time.LocalDateTime

class MemberEntityFactory {
    companion object {
        fun member(
            oAuthProvider: Member.OAuthProvider = Member.OAuthProvider.KAKAO,
            oauthId: String = "oauthId",
            nickname: String = "nickname",
            profileImageUrl: String? = "profileImageUrl",
            withdrawalYn: String = "N",
            withdrawalAt: LocalDateTime? = null,
        ) = Member(
            oauthProvider = oAuthProvider,
            oauthId = oauthId,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            withdrawalYn = withdrawalYn,
            withdrawalAt = withdrawalAt,
        )

        fun memberDevice(
            deviceId: String = "deviceId",
            member: Member = member(),
            osType: MemberDevice.OsType = MemberDevice.OsType.ANDROID,
            deviceModel: String = "deviceModel",
            appVersion: String = "appVersion",
            osVersion: String = "osVersion",
            activeYn: String = "Y",
        ) = MemberDevice(
            deviceId = deviceId,
            member = member,
            osType = osType,
            deviceModel = deviceModel,
            appVersion = appVersion,
            osVersion = osVersion,
            activeYn = activeYn,
        )
    }
}