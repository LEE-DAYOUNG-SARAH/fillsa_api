package store.fillsa.fillsa_api.domain.members.member.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice
import store.fillsa.fillsa_api.domain.members.member.repository.MemberDeviceRepository
import store.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import store.fillsa.fillsa_api.fixture.member.entity.MemberEntityFactory
import store.fillsa.fillsa_api.fixture.member.persist.MemberPersistFactory

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MemberServiceTest @Autowired constructor(
    val memberRepository: MemberRepository,
    val memberDeviceRepository: MemberDeviceRepository,
    val memberPersistFactory: MemberPersistFactory,
    val sut: MemberService
) {
    @Test
    fun `회원가입 성공 - 기존 회원`() {
        // given
        val existedUser = memberPersistFactory.createMember()
        assertThat(memberRepository.count()).isEqualTo(1)

        val existedDevice = memberPersistFactory.createMemberDevice(
            MemberEntityFactory.memberDevice(member = existedUser)
        )
        assertThat(memberDeviceRepository.count()).isEqualTo(1)

        // when
        val userData = LoginRequest.UserData(
            oAuthProvider = existedUser.oauthProvider,
            oAuthId = existedUser.oauthId,
            nickname = "ignoredNick",
            profileImageUrl = "ignoredUrl"
        )
        val deviceData = LoginRequest.DeviceData(
            deviceId = existedDevice.deviceId,
            osType = existedDevice.osType,
            appVersion = "newAppVersion",
            osVersion = "newOsVersion",
            deviceModel = existedDevice.deviceModel
        )
        val resultMember = sut.signUp(LoginRequest.LoginData(userData, deviceData))

        // then
        assertThat(resultMember.memberSeq).isEqualTo(existedUser.memberSeq)
        assertThat(resultMember.nickname).isEqualTo(existedUser.nickname)
        assertThat(resultMember.profileImageUrl).isEqualTo(existedUser.profileImageUrl)
        assertThat(memberRepository.count()).isEqualTo(1)

        val resultMemberDevice = memberDeviceRepository.findByIdOrNull(existedDevice.memberDeviceSeq)
        assertThat(resultMemberDevice?.appVersion).isEqualTo(existedDevice.appVersion)
        assertThat(resultMemberDevice?.osVersion).isEqualTo(existedDevice.osVersion)
        assertThat(memberDeviceRepository.count()).isEqualTo(1)
    }

    @Test
    fun `회원가입 성공 - 신규 회원`() {
        // given
        assertThat(memberRepository.count()).isEqualTo(0)
        assertThat(memberDeviceRepository.count()).isEqualTo(0)

        // when
        val userData = LoginRequest.UserData(
            oAuthProvider = Member.OAuthProvider.GOOGLE,
            oAuthId = "new-oauth-456",
            nickname = "newNick",
            profileImageUrl = "newUrl"
        )
        val deviceData = LoginRequest.DeviceData(
            deviceId = "deviceId",
            osType = MemberDevice.OsType.ANDROID,
            appVersion = "newAppVersion",
            osVersion = "newOsVersion",
            deviceModel = "newDeviceModel"
        )
        val resultMember = sut.signUp(LoginRequest.LoginData(userData, deviceData))

        // then
        assertThat(resultMember.memberSeq).isNotNull()
        assertThat(resultMember.oauthId).isEqualTo(userData.oAuthId)
        assertThat(resultMember.oauthProvider).isEqualTo(userData.oAuthProvider)
        assertThat(resultMember.nickname).isEqualTo(userData.nickname)
        assertThat(resultMember.profileImageUrl).isEqualTo(userData.profileImageUrl)
        assertThat(memberRepository.count()).isEqualTo(1)

        val resultMemberDevice = memberDeviceRepository.findAll()
        assertThat(resultMemberDevice).hasSize(1)
        assertThat(resultMemberDevice[0].member.memberSeq).isEqualTo(resultMember.memberSeq)
        assertThat(resultMemberDevice[0].deviceId).isEqualTo(deviceData.deviceId)
        assertThat(resultMemberDevice[0].osType).isEqualTo(deviceData.osType)
        assertThat(resultMemberDevice[0].appVersion).isEqualTo(deviceData.appVersion)
        assertThat(resultMemberDevice[0].osVersion).isEqualTo(deviceData.osVersion)
        assertThat(resultMemberDevice[0].deviceModel).isEqualTo(deviceData.deviceModel)
    }
}