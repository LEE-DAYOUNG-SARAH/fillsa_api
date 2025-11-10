package store.fillsa.fillsa_api.fixture.member.persist

import org.springframework.stereotype.Component
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice
import store.fillsa.fillsa_api.domain.members.member.repository.MemberDeviceRepository
import store.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberStreak
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberStreakRepository
import store.fillsa.fillsa_api.fixture.member.entity.MemberEntityFactory
import store.fillsa.fillsa_api.fixture.quote.entity.QuoteEntityFactory

@Component
class MemberPersistFactory(
    private val memberRepository: MemberRepository,
    private val memberDeviceRepository: MemberDeviceRepository,
    private val memberStreakRepository: MemberStreakRepository
) {
    fun createMember(member: Member = MemberEntityFactory.member()): Member {
        return memberRepository.save(member)
    }

    fun createMemberDevice(memberDevice: MemberDevice = MemberEntityFactory.memberDevice()): MemberDevice {
        return memberDeviceRepository.save(memberDevice)
    }

    fun createMemberWithStreak(
        member: Member = MemberEntityFactory.member(),
        memberStreak: MemberStreak? = null
    ): Pair<Member, MemberStreak> {
        val savedMember = createMember(member)
        val savedStreak = if (memberStreak != null) {
            memberStreakRepository.save(
                QuoteEntityFactory.memberStreak(
                    member = savedMember,
                    currentStreak = memberStreak.currentStreak,
                    maxStreak = memberStreak.maxStreak,
                    lastWrittenDate = memberStreak.lastWrittenDate
                )
            )
        } else {
            memberStreakRepository.save(
                QuoteEntityFactory.memberStreak(member = savedMember)
            )
        }
        return savedMember to savedStreak
    }
}