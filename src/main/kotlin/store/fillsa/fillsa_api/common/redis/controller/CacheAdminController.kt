package store.fillsa.fillsa_api.common.redis.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import store.fillsa.fillsa_api.common.redis.dto.DailyQuoteCacheRequest
import store.fillsa.fillsa_api.common.redis.dto.DailyQuoteCacheResponse
import store.fillsa.fillsa_api.common.redis.service.DailyQuoteCacheService
import java.time.LocalDate

@RestController
@RequestMapping("/api/admin/cache")
@Tag(name = "캐시 관리 API (Admin)")
class CacheAdminController(
    private val dailyQuoteCacheService: DailyQuoteCacheService
) {

    @PostMapping("/daily-quotes/reload")
    @Operation(summary = "명언 캐시 새로고침", description = "명언 데이터를 Redis에 다시 로딩합니다.")
    fun reloadQuotes(
        @RequestBody request: DailyQuoteCacheRequest
    ): ResponseEntity<List<DailyQuoteCacheResponse>> {
        return ResponseEntity.ok(
            dailyQuoteCacheService.reloadQuotes(request)
        )
    }

    @PostMapping("/daily-quotes/refresh/{date}")
    @Operation(summary = "특정 날짜 명언 캐시 갱신", description = "특정 날짜의 명언 캐시를 갱신합니다.")
    fun refreshQuote(
        @PathVariable @Parameter(description = "갱신할 날짜", example = "2024-09-12")
        date: LocalDate
    ): ResponseEntity<DailyQuoteCacheResponse> {
        return ResponseEntity.ok(
            dailyQuoteCacheService.refreshQuote(date)
        )
    }

    @GetMapping("/daily-quotes")
    @Operation(summary = "명언 캐시 조회", description = "현재 Redis에 캐시된 명언 데이터를 조회합니다.")
    fun getQuotes(
        @RequestBody request: DailyQuoteCacheRequest
    ): ResponseEntity<List<DailyQuoteCacheResponse>> {
        return ResponseEntity.ok(
            dailyQuoteCacheService.getQuotes(request)
        )
    }

    @DeleteMapping("/daily-quotes")
    @Operation(summary = "명언 캐시 삭제", description = "현재 Redis에 캐시된 명언 데이터를 삭제합니다.")
    fun deleteQuotes(
        @RequestBody request: DailyQuoteCacheRequest
    ) {
        dailyQuoteCacheService.deleteQuotes(request)
    }
}