package com.fillsa.admin.service.memberstreak

import com.fillsa.service.member.MemberStreak
import java.time.LocalDate

data class MemberStreakListItem(
    val memberSeq: Long,
    val nickname: String?,
    val currentStreak: Int,
    val maxStreak: Int,
    val lastWrittenDate: LocalDate?,
    val todayWritten: Boolean,
) {
    companion object {
        fun from(memberStreak: MemberStreak, today: LocalDate) = MemberStreakListItem(
            memberSeq = memberStreak.member.memberSeq,
            nickname = memberStreak.member.nickname,
            currentStreak = memberStreak.currentStreakAsOf(today),
            maxStreak = memberStreak.maxStreak,
            lastWrittenDate = memberStreak.lastWrittenDate,
            todayWritten = memberStreak.isTodayWritten(today),
        )
    }
}

data class StreakSummaryResponse(
    val maxStreak: Int,
    val avgCurrentStreak: Double,
    val todayWrittenCount: Int,
    val over30Count: Int,
) {
    companion object {
        fun from(
            maxStreak: Int,
            avgCurrentStreak: Double,
            todayWrittenCount: Long,
            over30Count: Long,
        ) = StreakSummaryResponse(
            maxStreak = maxStreak,
            avgCurrentStreak = avgCurrentStreak,
            todayWrittenCount = todayWrittenCount.toInt(),
            over30Count = over30Count.toInt(),
        )
    }
}
