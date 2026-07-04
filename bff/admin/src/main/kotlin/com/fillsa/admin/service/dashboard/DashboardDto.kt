package com.fillsa.admin.service.dashboard

import java.time.LocalDate

data class DashboardSummaryResponse(
    val totalMembers: Long,
    val newMembersThisMonth: Int,
    val todayCompletedCount: Int,
    val totalQuotes: Long,
) {
    companion object {
        fun from(
            totalMembers: Long,
            newMembersThisMonth: Long,
            todayCompletedCount: Long,
            totalQuotes: Long,
        ) = DashboardSummaryResponse(
            totalMembers = totalMembers,
            newMembersThisMonth = newMembersThisMonth.toInt(),
            todayCompletedCount = todayCompletedCount.toInt(),
            totalQuotes = totalQuotes,
        )
    }
}

data class CompletionTrendResponse(
    val days: List<CompletionTrendDay>,
) {
    companion object {
        fun from(days: List<CompletionTrendDay>) = CompletionTrendResponse(days = days)
    }
}

data class CompletionTrendDay(
    val date: LocalDate,
    val completedCount: Int,
)

data class StreakDistributionResponse(
    val buckets: List<StreakBucket>,
    val maxStreak: Int,
) {
    companion object {
        fun from(buckets: List<StreakBucket>, maxStreak: Int) = StreakDistributionResponse(
            buckets = buckets,
            maxStreak = maxStreak,
        )
    }
}

data class StreakBucket(
    val label: String,
    val count: Int,
)

/**
 * 최근 N일 완료 추이 집계용 JPQL 생성자 표현식 프로젝션 (member_quotes.TODAY_COMPLETED 일별 집계).
 * 데이터가 없는 날짜는 쿼리 결과에서 아예 빠지므로 서비스 레이어에서 0으로 채운다.
 */
data class DailyCompletionCount(
    val quoteDate: LocalDate,
    val completedCount: Long,
)
