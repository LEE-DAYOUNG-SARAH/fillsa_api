package com.fillsa.app.api.members.quote

import io.swagger.v3.oas.annotations.media.Schema
import com.fillsa.service.member.MemberStreak
import java.time.LocalDate

data class MemberStreakResponse(
    @Schema(description = "연속 필사 일수", required = true)
    val currentStreak: Int,

    @Schema(description = "오늘 필사 여부", required = true)
    val isTodayWritten: Boolean
) {
    companion object {
        fun from(memberStreak: MemberStreak, quoteDate: LocalDate) = MemberStreakResponse(
            currentStreak = memberStreak.currentStreakAsOf(quoteDate),
            isTodayWritten = memberStreak.isTodayWritten(quoteDate)
        )
    }
}
