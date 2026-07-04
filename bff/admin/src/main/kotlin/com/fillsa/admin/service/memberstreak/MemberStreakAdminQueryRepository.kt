package com.fillsa.admin.service.memberstreak

import com.fillsa.service.member.MemberStreak
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface MemberStreakAdminQueryRepository : JpaRepository<MemberStreak, Long> {

    /**
     * 어드민 연속 필사 랭킹. keyword 는 회원 닉네임 부분일치(대소문자 무시). null 이면 무시된다.
     */
    @Query(
        value = """
        select ms
        from MemberStreak ms
            join fetch ms.member m
        where (:keyword is null or lower(m.nickname) like lower(concat('%', :keyword, '%')))
        """,
        countQuery = """
        select count(ms)
        from MemberStreak ms
            join ms.member m
        where (:keyword is null or lower(m.nickname) like lower(concat('%', :keyword, '%')))
        """,
    )
    fun search(@Param("keyword") keyword: String?, pageable: Pageable): Page<MemberStreak>

    @Query("select coalesce(max(ms.maxStreak), 0) from MemberStreak ms")
    fun findMaxStreak(): Int

    /**
     * 조회 시점(today) 기준 보정된 currentStreak(lastWrittenDate 가 today/yesterday 가 아니면 0)의 평균.
     */
    @Query(
        """
        select coalesce(
            avg(case when ms.lastWrittenDate = :today or ms.lastWrittenDate = :yesterday then ms.currentStreak else 0 end),
            0.0
        )
        from MemberStreak ms
        """,
    )
    fun findAvgCurrentStreak(@Param("today") today: LocalDate, @Param("yesterday") yesterday: LocalDate): Double

    fun countByLastWrittenDate(lastWrittenDate: LocalDate): Long

    /**
     * 조회 시점 기준 보정된 currentStreak 이 30일 이상 유지되는 회원 수.
     */
    @Query(
        """
        select count(ms)
        from MemberStreak ms
        where (case when ms.lastWrittenDate = :today or ms.lastWrittenDate = :yesterday then ms.currentStreak else 0 end) >= 30
        """,
    )
    fun countOver30(@Param("today") today: LocalDate, @Param("yesterday") yesterday: LocalDate): Long
}
