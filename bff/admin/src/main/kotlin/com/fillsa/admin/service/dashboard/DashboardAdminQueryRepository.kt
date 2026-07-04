package com.fillsa.admin.service.dashboard

import com.fillsa.service.member.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 대시보드 KPI/차트용 읽기 전용 집계 리포지토리. member/quote 도메인을 가로지르는 집계이므로
 * 도메인별 조회 리포지토리(MemberAdminQueryRepository 등)와 분리해 dashboard 패키지 하나에 모은다.
 *
 * 제네릭 엔티티는 Member 로 고정했지만(내장 count() 재사용), 아래 커스텀 @Query 메서드들은
 * Quote/MemberQuote/MemberStreak 등 다른 엔티티도 자유롭게 조회한다 — Spring Data JPA 는
 * @Query 의 FROM 절과 리포지토리 제네릭 타입의 일치를 요구하지 않는다.
 *
 * 🔴 이름 주의: 이 인터페이스의 simple name(DashboardAdminQueryRepository)은 다른 패키지의
 * 리포지토리와 절대 겹치면 안 된다(겹치면 BeanDefinitionOverrideException). 모든 대시보드 집계
 * 쿼리를 이 리포지토리 하나에 모아 이름 충돌 가능성 자체를 없앤다.
 */
interface DashboardAdminQueryRepository : JpaRepository<Member, Long> {

    /** 이번 달(1일 00:00 ~ 현재) 신규 가입 회원 수 — 전체 회원 수 카드의 증감 지표. */
    @Query("select count(m) from Member m where m.createdAt >= :monthStart")
    fun countNewMembersSince(@Param("monthStart") monthStart: LocalDateTime): Long

    /** 등록된 명언 수 (soft delete 제외, DEL_YN = 'N'). */
    @Query("select count(q) from Quote q where q.delYn = 'N'")
    fun countActiveQuotes(): Long

    /**
     * 특정 날짜(quoteDate)에 실시간 완료(TODAY_COMPLETED = true)된 필사 건수.
     * KPI 카드의 "오늘 필사 완료 수"는 date = 오늘로 호출한다.
     */
    @Query(
        """
        select count(mq)
        from MemberQuote mq
            join mq.dailyQuote dq
        where dq.quoteDate = :date
            and mq.todayCompleted = true
        """,
    )
    fun countTodayCompletedByDate(@Param("date") date: LocalDate): Long

    /**
     * 기간 내 날짜별 실시간 완료(TODAY_COMPLETED = true) 건수. 완료 건이 없는 날짜는
     * 결과 목록에서 아예 빠지므로 서비스 레이어에서 0으로 채워야 한다.
     */
    @Query(
        """
        select new com.fillsa.admin.service.dashboard.DailyCompletionCount(dq.quoteDate, count(mq))
        from MemberQuote mq
            join mq.dailyQuote dq
        where dq.quoteDate between :startDate and :endDate
            and mq.todayCompleted = true
        group by dq.quoteDate
        """,
    )
    fun countTodayCompletedGroupByDate(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<DailyCompletionCount>

    /** 전체 회원 중 최장 연속 필사 기록. */
    @Query("select coalesce(max(ms.maxStreak), 0) from MemberStreak ms")
    fun findMaxStreak(): Int

    /**
     * 조회 시점(today) 기준 보정된 currentStreak(lastWrittenDate 가 today/yesterday 가 아니면 0)이
     * 30일 이상인 회원 수 — MemberStreakAdminQueryRepository.countOver30 과 동일한 보정 로직.
     */
    @Query(
        """
        select count(ms)
        from MemberStreak ms
        where (case when ms.lastWrittenDate = :today or ms.lastWrittenDate = :yesterday then ms.currentStreak else 0 end) >= 30
        """,
    )
    fun countStreakOver30(@Param("today") today: LocalDate, @Param("yesterday") yesterday: LocalDate): Long

    @Query(
        """
        select count(ms)
        from MemberStreak ms
        where (case when ms.lastWrittenDate = :today or ms.lastWrittenDate = :yesterday then ms.currentStreak else 0 end) between 14 and 29
        """,
    )
    fun countStreak14To29(@Param("today") today: LocalDate, @Param("yesterday") yesterday: LocalDate): Long

    @Query(
        """
        select count(ms)
        from MemberStreak ms
        where (case when ms.lastWrittenDate = :today or ms.lastWrittenDate = :yesterday then ms.currentStreak else 0 end) between 7 and 13
        """,
    )
    fun countStreak7To13(@Param("today") today: LocalDate, @Param("yesterday") yesterday: LocalDate): Long

    @Query(
        """
        select count(ms)
        from MemberStreak ms
        where (case when ms.lastWrittenDate = :today or ms.lastWrittenDate = :yesterday then ms.currentStreak else 0 end) between 1 and 6
        """,
    )
    fun countStreak1To6(@Param("today") today: LocalDate, @Param("yesterday") yesterday: LocalDate): Long

    @Query(
        """
        select count(ms)
        from MemberStreak ms
        where (case when ms.lastWrittenDate = :today or ms.lastWrittenDate = :yesterday then ms.currentStreak else 0 end) = 0
        """,
    )
    fun countStreak0(@Param("today") today: LocalDate, @Param("yesterday") yesterday: LocalDate): Long
}
