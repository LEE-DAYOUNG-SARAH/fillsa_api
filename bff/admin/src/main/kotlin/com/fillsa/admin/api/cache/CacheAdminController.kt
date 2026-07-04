package com.fillsa.admin.api.cache

import com.fillsa.admin.service.cache.CacheAdminService
import com.fillsa.admin.service.cache.CacheRefreshRequest
import com.fillsa.admin.service.cache.CacheRefreshResponse
import com.fillsa.admin.service.cache.CachedDailyQuotesResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/v1/cache")
@Tag(name = "cache", description = "캐시 관리")
class CacheAdminController(
    private val cacheAdminService: CacheAdminService,
) {

    @GetMapping("/daily-quotes")
    @Operation(summary = "캐시된 일별 명언 미리보기")
    fun getCachedDailyQuotes(): ResponseEntity<CachedDailyQuotesResponse> =
        ResponseEntity.ok(cacheAdminService.preview())

    @PostMapping("/daily-quotes/refresh")
    @Operation(summary = "캐시 갱신 (date 없으면 전체)")
    fun refreshDailyQuoteCache(
        @RequestBody(required = false) request: CacheRefreshRequest?,
    ): ResponseEntity<CacheRefreshResponse> =
        ResponseEntity.ok(cacheAdminService.refresh(request ?: CacheRefreshRequest()))

    @DeleteMapping("/daily-quotes")
    @Operation(summary = "캐시 전체 삭제 (다음 요청 시 DB 재로딩)")
    fun evictDailyQuoteCache(): ResponseEntity<Void> {
        cacheAdminService.evict()
        return ResponseEntity.noContent().build()
    }
}
