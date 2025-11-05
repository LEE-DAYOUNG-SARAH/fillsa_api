package store.fillsa.fillsa_api.migration

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import store.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberStreak
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberStreakRepository

@Disabled
@ActiveProfiles("test")
@SpringBootTest
class MemberStreakMigration @Autowired constructor(
    val memberRepository: MemberRepository,
    val memberStreakRepository: MemberStreakRepository
) {

    @Test
    fun `모든 회원에 대한 MemberStreak 초기 데이터 생성`() {
        val allMembers = memberRepository.findAll()

        println("총 ${allMembers.size}명의 회원에 대해 MemberStreak 생성 시작")

        val memberStreaks = allMembers.mapNotNull { member ->
            // 이미 존재하는지 확인
            val existing = memberStreakRepository.findByMember(member)
            if (existing != null) {
                println("회원 ${member.memberSeq} - 이미 MemberStreak 존재, 스킵")
                null
            } else {
                println("회원 ${member.memberSeq} - MemberStreak 생성")
                MemberStreak(
                    member = member,
                    currentStreak = 0,
                    maxStreak = 0,
                    lastWrittenDate = null
                )
            }
        }

        memberStreakRepository.saveAll(memberStreaks)

        println("완료: ${memberStreaks.size}개의 MemberStreak 생성됨")
        println("스킵: ${allMembers.size - memberStreaks.size}개 (기존 존재)")
    }
}