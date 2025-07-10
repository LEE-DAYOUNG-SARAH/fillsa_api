package store.fillsa.fillsa_api.domain.members.member.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice
import store.fillsa.fillsa_api.domain.members.member.repository.MemberDeviceRepository
import store.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import store.fillsa.fillsa_api.fixture.member.entity.MemberEntityFactory
import store.fillsa.fillsa_api.fixture.member.persist.MemberPersistFactory

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberDeviceServiceTest @Autowired constructor(
    private val memberPersistFactory: MemberPersistFactory,
    private val memberDeviceRepository: MemberDeviceRepository,
    private val memberRepository: MemberRepository,
    private val sut: MemberDeviceService
) {
    
    @Test
    fun `디바이스 생성 성공 - 신규 디바이스인 경우 새로 생성한다`() {
        // given
        val member = memberRepository.save(MemberEntityFactory.member())
        val deviceData = LoginRequest.DeviceData(
            deviceId = "new-device-id",
            osType = MemberDevice.OsType.ANDROID,
            appVersion = "1.0.0",
            osVersion = "10.0.0",
            deviceModel = "Test Device"
        )
        
        // when
        val result = sut.create(member, deviceData)
        
        // then
        assertThat(result.deviceId).isEqualTo(deviceData.deviceId)
        assertThat(result.member).isEqualTo(member)
        assertThat(result.osType).isEqualTo(MemberDevice.OsType.ANDROID)
        assertThat(result.appVersion).isEqualTo(deviceData.appVersion)
        assertThat(result.osVersion).isEqualTo(deviceData.osVersion)
        assertThat(result.deviceModel).isEqualTo(deviceData.deviceModel)
        assertThat(result.activeYn).isEqualTo("Y")
    }
    
    @Test
    fun `디바이스 생성 성공 - 기존 디바이스인 경우 업데이트한다`() {
        // given
        val member = memberRepository.save(MemberEntityFactory.member())
        val existingMemberDevice = memberPersistFactory.createMemberDevice(MemberEntityFactory.memberDevice(
            deviceId = "existing-device-id",
            member = member,
            osType = MemberDevice.OsType.ANDROID,
            appVersion = "1.0.0",
            osVersion = "10.0.0",
            deviceModel = "Test Device"
        ))
        
        val deviceData = LoginRequest.DeviceData(
            deviceId = "existing-device-id",
            osType = MemberDevice.OsType.ANDROID,
            appVersion = "1.1.0",
            osVersion = "11.0.0",
            deviceModel = "Test Device"
        )
        
        // when
        val result = sut.create(member, deviceData)
        
        // then
        assertThat(result.deviceId).isEqualTo(deviceData.deviceId)
        assertThat(result.appVersion).isEqualTo(deviceData.appVersion)
        assertThat(result.osVersion).isEqualTo(deviceData.osVersion)
        assertThat(result.deviceModel).isEqualTo(deviceData.deviceModel)
    }
    
    @Test
    fun `로그아웃 성공 - 기존 디바이스인 경우 로그아웃한다`() {
        // given
        val member = memberRepository.save(MemberEntityFactory.member())
        val deviceId = "existing-device-id"
        val existingMemberDevice = memberPersistFactory.createMemberDevice(MemberEntityFactory.memberDevice(
            deviceId = deviceId,
            member = member
        ))
        
        // when
        sut.logout(member, deviceId)
        
        // then
        val updatedDevice = memberDeviceRepository.findByMemberAndDeviceId(member, deviceId)
        assertThat(updatedDevice?.activeYn).isEqualTo("N")
    }
    
    @Test
    fun `로그아웃 성공 - 존재하지 않는 디바이스인 경우 아무것도 하지 않는다`() {
        // given
        val member = memberRepository.save(MemberEntityFactory.member())
        val deviceId = "non-existing-device-id"
        
        // when
        sut.logout(member, deviceId)
        
        // then
        val result = memberDeviceRepository.findByMemberAndDeviceId(member, deviceId)
        assertThat(result).isNull()
    }
} 