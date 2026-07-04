package com.fillsa.admin.service.dashboard

import com.fillsa.admin.fixture.AdminFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardAdminServiceTest @Autowired constructor(
    private val sut: DashboardAdminService,
    private val adminFixtures: AdminFixtures,
) {

    @Test
    fun `요약 - 전체 회원수, 이번달 신규 회원, 오늘 완료 수, 등록된 명언 수(삭제 제외)`() {
        adminFixtures.member(nickname = "회원1")
        adminFixtures.member(nickname = "회원2")

        adminFixtures.quote(korQuote = "활성 명언1")
        val activeQuote2 = adminFixtures.quote(korQuote = "활성 명언2")
        adminFixtures.quote(korQuote = "삭제된 명언", delYn = "Y")

        val today = LocalDate.now()
        val dailyQuote = adminFixtures.dailyQuote(activeQuote2, today)
        val completedMember = adminFixtures.member(nickname = "완료회원")
        val notCompletedMember = adminFixtures.member(nickname = "미완료회원")
        adminFixtures.memberQuote(completedMember, dailyQuote, todayCompleted = true)
        adminFixtures.memberQuote(notCompletedMember, dailyQuote, todayCompleted = false)

        val summary = sut.summary()

        // 이번 테스트에서 생성한 회원은 모두 "지금" 생성되므로 이번 달 신규 회원 = 전체 회원 수
        assertThat(summary.totalMembers).isEqualTo(4)
        assertThat(summary.newMembersThisMonth).isEqualTo(4)
        assertThat(summary.todayCompletedCount).isEqualTo(1)
        assertThat(summary.totalQuotes).isEqualTo(2)
    }

    @Test
    fun `완료 추이 - N일 구간을 오늘 기준으로 데이터 없는 날짜는 0으로 채워 반환한다`() {
        val today = LocalDate.now()
        val twoDaysAgo = today.minusDays(2)

        val quote = adminFixtures.quote(korQuote = "명언")
        val todayDailyQuote = adminFixtures.dailyQuote(quote, today)
        val pastDailyQuote = adminFixtures.dailyQuote(quote, twoDaysAgo)

        val member1 = adminFixtures.member(nickname = "회원1")
        val member2 = adminFixtures.member(nickname = "회원2")
        val member3 = adminFixtures.member(nickname = "회원3")

        adminFixtures.memberQuote(member1, todayDailyQuote, todayCompleted = true)
        adminFixtures.memberQuote(member2, todayDailyQuote, todayCompleted = true)
        adminFixtures.memberQuote(member3, pastDailyQuote, todayCompleted = true)

        val trend = sut.completionTrend(3)

        assertThat(trend.days).hasSize(3)
        assertThat(trend.days.map { it.date }).containsExactly(twoDaysAgo, today.minusDays(1), today)
        assertThat(trend.days[0].completedCount).isEqualTo(1)
        assertThat(trend.days[1].completedCount).isEqualTo(0)
        assertThat(trend.days[2].completedCount).isEqualTo(2)
    }

    @Test
    fun `완료 추이 - days 는 1~90 사이로 clamp 되고 오늘 날짜에 고정된다`() {
        val today = LocalDate.now()

        val trendMin = sut.completionTrend(0)
        assertThat(trendMin.days).hasSize(1)
        assertThat(trendMin.days[0].date).isEqualTo(today)

        val trendMax = sut.completionTrend(1000)
        assertThat(trendMax.days).hasSize(90)
        assertThat(trendMax.days.first().date).isEqualTo(today.minusDays(89))
        assertThat(trendMax.days.last().date).isEqualTo(today)
    }

    @Test
    fun `연속 분포 - 5개 버킷은 today,yesterday 아니면 0으로 보정되고, 최장 기록은 보정 없이 유지된다`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        adminFixtures.memberStreak(
            adminFixtures.member(nickname = "30일이상"),
            currentStreak = 35,
            maxStreak = 35,
            lastWrittenDate = today,
        )
        adminFixtures.memberStreak(
            adminFixtures.member(nickname = "14~29일"),
            currentStreak = 20,
            maxStreak = 20,
            lastWrittenDate = yesterday,
        )
        adminFixtures.memberStreak(
            adminFixtures.member(nickname = "7~13일"),
            currentStreak = 10,
            maxStreak = 10,
            lastWrittenDate = today,
        )
        adminFixtures.memberStreak(
            adminFixtures.member(nickname = "1~6일"),
            currentStreak = 3,
            maxStreak = 3,
            lastWrittenDate = today,
        )
        adminFixtures.memberStreak(
            adminFixtures.member(nickname = "미작성"),
            currentStreak = 0,
            maxStreak = 0,
            lastWrittenDate = null,
        )
        adminFixtures.memberStreak(
            adminFixtures.member(nickname = "끊긴최장기록"),
            currentStreak = 50,
            maxStreak = 50,
            lastWrittenDate = today.minusDays(3),
        )

        val distribution = sut.streakDistribution()

        val countByLabel = distribution.buckets.associate { it.label to it.count }
        assertThat(countByLabel["30+"]).isEqualTo(1)
        assertThat(countByLabel["14-29"]).isEqualTo(1)
        assertThat(countByLabel["7-13"]).isEqualTo(1)
        assertThat(countByLabel["1-6"]).isEqualTo(1)
        // "미작성" + "끊긴최장기록"(그제 이전에 끊겨 0으로 보정) = 2
        assertThat(countByLabel["0"]).isEqualTo(2)
        // maxStreak 는 보정 없이 회원별 역대 최고 기록 중 최댓값이므로 끊긴 회원의 50이 그대로 반영된다
        assertThat(distribution.maxStreak).isEqualTo(50)
    }
}
