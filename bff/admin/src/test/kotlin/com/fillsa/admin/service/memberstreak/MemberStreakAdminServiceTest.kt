package com.fillsa.admin.service.memberstreak

import com.fillsa.admin.fixture.AdminFixtures
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberStreakAdminServiceTest @Autowired constructor(
    private val sut: MemberStreakAdminService,
    private val adminFixtures: AdminFixtures,
) {

    @Test
    fun `목록 - 기본 정렬 currentStreak desc`() {
        val member1 = adminFixtures.member(nickname = "낮은연속")
        val member2 = adminFixtures.member(nickname = "높은연속")
        adminFixtures.memberStreak(member1, currentStreak = 3, maxStreak = 3, lastWrittenDate = LocalDate.now())
        adminFixtures.memberStreak(member2, currentStreak = 10, maxStreak = 10, lastWrittenDate = LocalDate.now())

        val page = sut.list(PageRequest.of(0, 10), null)

        assertThat(page.content).hasSize(2)
        assertThat(page.content[0].nickname).isEqualTo("높은연속")
        assertThat(page.content[1].nickname).isEqualTo("낮은연속")
        assertThat(page.page.totalElements).isEqualTo(2)
    }

    @Test
    fun `목록 - 닉네임 부분일치 검색`() {
        val member1 = adminFixtures.member(nickname = "행복러너")
        val member2 = adminFixtures.member(nickname = "슬픔러너")
        adminFixtures.memberStreak(member1, currentStreak = 5, lastWrittenDate = LocalDate.now())
        adminFixtures.memberStreak(member2, currentStreak = 3, lastWrittenDate = LocalDate.now())

        val page = sut.list(PageRequest.of(0, 10), "행복")

        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].nickname).isEqualTo("행복러너")
    }

    @Test
    fun `목록 - 페이징`() {
        repeat(3) { i ->
            val member = adminFixtures.member(nickname = "연속회원$i")
            adminFixtures.memberStreak(member, currentStreak = i, lastWrittenDate = LocalDate.now())
        }

        val page = sut.list(PageRequest.of(0, 2), null)

        assertThat(page.content).hasSize(2)
        assertThat(page.page.totalElements).isEqualTo(3)
        assertThat(page.page.totalPages).isEqualTo(2)
    }

    @Test
    fun `목록 - 오늘 작성했다면 currentStreak 유지, todayWritten true`() {
        val member = adminFixtures.member(nickname = "오늘작성")
        adminFixtures.memberStreak(member, currentStreak = 4, maxStreak = 4, lastWrittenDate = LocalDate.now())

        val page = sut.list(PageRequest.of(0, 10), null)

        assertThat(page.content[0].currentStreak).isEqualTo(4)
        assertThat(page.content[0].todayWritten).isTrue()
    }

    @Test
    fun `목록 - 어제까지 연속이면 currentStreak 유지, todayWritten false`() {
        val member = adminFixtures.member(nickname = "어제까지연속")
        adminFixtures.memberStreak(member, currentStreak = 7, maxStreak = 7, lastWrittenDate = LocalDate.now().minusDays(1))

        val page = sut.list(PageRequest.of(0, 10), null)

        assertThat(page.content[0].currentStreak).isEqualTo(7)
        assertThat(page.content[0].todayWritten).isFalse()
    }

    @Test
    fun `목록 - 그제 이전에 끊긴 연속은 currentStreak 0으로 보정된다`() {
        val member = adminFixtures.member(nickname = "끊긴연속")
        adminFixtures.memberStreak(member, currentStreak = 7, maxStreak = 7, lastWrittenDate = LocalDate.now().minusDays(3))

        val page = sut.list(PageRequest.of(0, 10), null)

        assertThat(page.content[0].currentStreak).isEqualTo(0)
        assertThat(page.content[0].maxStreak).isEqualTo(7)
        assertThat(page.content[0].todayWritten).isFalse()
    }

    @Test
    fun `요약 - 최장 연속, 평균 연속(보정치), 오늘 작성 수, 30일 이상 유지 수`() {
        val today = LocalDate.now()

        val longStreakMember = adminFixtures.member(nickname = "장기연속")
        adminFixtures.memberStreak(longStreakMember, currentStreak = 40, maxStreak = 40, lastWrittenDate = today)

        val shortStreakMember = adminFixtures.member(nickname = "단기연속")
        adminFixtures.memberStreak(shortStreakMember, currentStreak = 5, maxStreak = 5, lastWrittenDate = today)

        val brokenStreakMember = adminFixtures.member(nickname = "끊긴연속")
        adminFixtures.memberStreak(
            brokenStreakMember,
            currentStreak = 20,
            maxStreak = 50,
            lastWrittenDate = today.minusDays(5),
        )

        val summary = sut.summary()

        assertThat(summary.maxStreak).isEqualTo(50)
        assertThat(summary.todayWrittenCount).isEqualTo(2)
        assertThat(summary.over30Count).isEqualTo(1)
        // 보정된 currentStreak 평균: (40 + 5 + 0(끊김 보정)) / 3 = 15.0
        assertThat(summary.avgCurrentStreak).isCloseTo(15.0, Offset.offset(0.001))
    }
}
