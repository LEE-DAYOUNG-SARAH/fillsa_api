package com.fillsa.admin.api.dailyquote

import com.fillsa.admin.service.dailyquote.DailyQuoteAdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/admin/v1/daily-quotes")
@Tag(name = "daily-quotes", description = "일별 명언 배정")
class DailyQuoteAdminController(
    private val dailyQuoteAdminService: DailyQuoteAdminService,
) {

    @GetMapping
    @Operation(summary = "월별 배정 현황 (캘린더용)")
    fun getDailyQuotes(@RequestParam yearMonth: String): ResponseEntity<DailyQuoteMonthResponse> =
        ResponseEntity.ok(dailyQuoteAdminService.monthView(yearMonth))

    @PutMapping("/{date}")
    @Operation(summary = "특정 날짜 명언 배정/변경 (quoteDayOfWeek 서버 자동 계산)")
    fun assignDailyQuote(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestBody request: AssignDailyQuoteRequest,
    ): ResponseEntity<DailyQuoteDayResponse> =
        ResponseEntity.ok(dailyQuoteAdminService.assign(date, request))

    @PostMapping("/auto-assign")
    @Operation(summary = "미배정 날짜 자동 배정 (미사용 명언 우선, 소진 시 배정 횟수 적은 순)")
    fun autoAssignDailyQuotes(@RequestBody request: AutoAssignRequest): ResponseEntity<AutoAssignResponse> =
        ResponseEntity.ok(dailyQuoteAdminService.autoAssign(request.yearMonth))
}
