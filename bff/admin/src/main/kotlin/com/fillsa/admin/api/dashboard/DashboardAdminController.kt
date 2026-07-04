package com.fillsa.admin.api.dashboard

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.fillsa.admin.service.dashboard.CompletionTrendResponse
import com.fillsa.admin.service.dashboard.DashboardAdminService
import com.fillsa.admin.service.dashboard.DashboardSummaryResponse
import com.fillsa.admin.service.dashboard.StreakDistributionResponse

@RestController
@RequestMapping("/api/admin/v1/dashboard")
@Tag(name = "dashboard", description = "대시보드")
class DashboardAdminController(
    private val dashboardAdminService: DashboardAdminService,
) {

    @GetMapping("/summary")
    @Operation(summary = "KPI 요약 (전체 회원 / 오늘 필사 완료 / 등록 명언)")
    fun getDashboardSummary(): ResponseEntity<DashboardSummaryResponse> =
        ResponseEntity.ok(dashboardAdminService.summary())

    @GetMapping("/completion-trend")
    @Operation(summary = "최근 N일 필사 완료 추이 (todayCompleted 기준 일별 집계)")
    fun getCompletionTrend(
        @RequestParam(defaultValue = "14") days: Int,
    ): ResponseEntity<CompletionTrendResponse> =
        ResponseEntity.ok(dashboardAdminService.completionTrend(days))

    @GetMapping("/streak-distribution")
    @Operation(summary = "연속 필사 분포 (currentStreak 구간별)")
    fun getStreakDistribution(): ResponseEntity<StreakDistributionResponse> =
        ResponseEntity.ok(dashboardAdminService.streakDistribution())
}
