package com.fillsa.admin.service.member

import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDevice
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.member.MemberStreak
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MemberAdminQueryRepository : JpaRepository<Member, Long> {

    /**
     * 어드민 회원 목록. keyword 는 닉네임·OAuth ID 부분일치, oauthProvider/withdrawalYn 은 정확일치.
     * 모든 조건이 null 이면 무시된다.
     */
    @Query(
        """
        select m
        from Member m
        where (:keyword is null
                or lower(m.nickname) like lower(concat('%', :keyword, '%'))
                or lower(m.oauthId) like lower(concat('%', :keyword, '%')))
            and (:oauthProvider is null or m.oauthProvider = :oauthProvider)
            and (:withdrawalYn is null or m.withdrawalYn = :withdrawalYn)
        """,
    )
    fun search(
        @Param("keyword") keyword: String?,
        @Param("oauthProvider") oauthProvider: Member.OAuthProvider?,
        @Param("withdrawalYn") withdrawalYn: String?,
        pageable: Pageable,
    ): Page<Member>

    /** 통계 카드(전체/활성/탈퇴)용. */
    fun countByWithdrawalYn(withdrawalYn: String): Long

    /** 통계 카드(팀원 수)용. */
    fun countByAdminYn(adminYn: String): Long
}

/**
 * 회원 목록·상세의 연속 필사 조회용 (member_streaks).
 * 이름은 memberstreak 패키지의 MemberStreakAdminQueryRepository 와의 Spring 빈 이름 충돌을 피하기 위해
 * 별도로 부여되었다 (동일 simple name 의 리포지토리가 다른 패키지에 존재하면 @EnableJpaRepositories 스캔 시
 * BeanDefinitionOverrideException 발생).
 */
interface MemberStreakLookupRepository : JpaRepository<MemberStreak, Long> {
    fun findByMember(member: Member): MemberStreak?

    fun findAllByMember_MemberSeqIn(memberSeqs: List<Long>): List<MemberStreak>
}

/**
 * 회원 상세의 누적 필사 완료 건수 조회용 (member_quotes).
 * 이름은 memberquote 패키지의 MemberQuoteAdminQueryRepository 와의 Spring 빈 이름 충돌을 피하기 위해
 * 별도로 부여되었다 (동일 simple name 의 리포지토리가 다른 패키지에 존재하면 @EnableJpaRepositories 스캔 시
 * BeanDefinitionOverrideException 발생).
 */
interface MemberQuoteCompletionRepository : JpaRepository<MemberQuote, Long> {
    fun countByMemberAndCompleted(member: Member, completed: Boolean): Long
}

/** 회원 상세의 등록 디바이스 목록 조회용 (member_devices). */
interface MemberDeviceAdminQueryRepository : JpaRepository<MemberDevice, Long> {
    fun findAllByMemberOrderByUpdatedAtDesc(member: Member): List<MemberDevice>
}
