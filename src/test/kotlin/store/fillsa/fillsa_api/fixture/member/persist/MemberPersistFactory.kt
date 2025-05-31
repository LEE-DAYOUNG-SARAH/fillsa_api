package store.fillsa.fillsa_api.fixture.member.persist

import org.springframework.stereotype.Component
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice
import store.fillsa.fillsa_api.domain.members.member.repository.MemberDeviceRepository
import store.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import store.fillsa.fillsa_api.fixture.member.entity.MemberEntityFactory

@Component
class MemberPersistFactory(
    private val memberRepository: MemberRepository,
    private val memberDeviceRepository: MemberDeviceRepository
) {
    fun createMember(member: Member = MemberEntityFactory.member()): Member {
        return memberRepository.save(member)
    }

    fun createMemberDevice(memberDevice: MemberDevice = MemberEntityFactory.memberDevice()): MemberDevice {
        return memberDeviceRepository.save(memberDevice)
    }
}