package com.fillsa.app.fixture.member.persist

import org.springframework.stereotype.Component
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDevice
import com.fillsa.service.member.MemberDeviceRepository
import com.fillsa.service.member.MemberRepository
import com.fillsa.service.member.MemberStreak
import com.fillsa.service.member.MemberStreakRepository
import com.fillsa.app.fixture.member.entity.MemberEntityFactory
import com.fillsa.app.fixture.quote.entity.QuoteEntityFactory

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