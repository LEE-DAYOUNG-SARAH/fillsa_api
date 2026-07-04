package com.fillsa.admin.service.dashboard

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class DashboardAdminService(
    private val dashboardAdminRepository: DashboardAdminQueryRepository,
) {

    @Transactional(readOnly = true)
    fun summary(): DashboardSummaryResponse {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).atStartOfDay()

        return DashboardSummaryResponse.from(
            totalMembers = dashboardAdminRepository.count(),
            newMembersThisMonth = dashboardAdminRepository.countNewMembersSince(monthStart),
            todayCompletedCount = dashboardAdminRepository.countTodayCompletedByDate(today),
            totalQuotes = dashboardAdminRepository.countActiveQuotes(),
        )
    }

    @Transactional(readOnly = true)
    fun completionTrend(days: Int): CompletionTrendResponse {
        val today = LocalDate.now()
        val effectiveDays = days.coerceIn(1, 90)
        val startDate = today.minusDays((effectiveDays - 1).toLong())

        val countByDate = dashboardAdminRepository
            .countTodayCompletedGroupByDate(startDate, today)
            .associate { it.quoteDate to it.completedCount }

        val trend = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }
            .map { date -> CompletionTrendDay(date = date, completedCount = (countByDate[date] ?: 0L).toInt()) }
            .toList()

        return CompletionTrendResponse.from(trend)
    }

    @Transactional(readOnly = true)
    fun streakDistribution(): StreakDistributionResponse {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val buckets = listOf(
            StreakBucket("30+", dashboardAdminRepository.countStreakOver30(today, yesterday).toInt()),
            StreakBucket("14-29", dashboardAdminRepository.countStreak14To29(today, yesterday).toInt()),
            StreakBucket("7-13", dashboardAdminRepository.countStreak7To13(today, yesterday).toInt()),
            StreakBucket("1-6", dashboardAdminRepository.countStreak1To6(today, yesterday).toInt()),
            StreakBucket("0", dashboardAdminRepository.countStreak0(today, yesterday).toInt()),
        )

        return StreakDistributionResponse.from(
            buckets = buckets,
            maxStreak = dashboardAdminRepository.findMaxStreak(),
        )
    }
}
