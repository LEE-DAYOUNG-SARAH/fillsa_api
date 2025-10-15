package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberStreak
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
