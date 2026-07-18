package com.fillsa.admin.api.memberstreak

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.admin.service.memberstreak.MemberStreakAdminService
import com.fillsa.admin.service.memberstreak.MemberStreakListItem
import com.fillsa.admin.service.memberstreak.StreakSummaryResponse

@RestController
@RequestMapping("/api/admin/v1/member-streaks")
@Tag(name = "member-streaks", description = "연속 필사 관리")
class MemberStreakAdminController(
    private val memberStreakAdminService: MemberStreakAdminService,
) {

    @GetMapping
    @Operation(summary = "연속 필사 랭킹 (기본 정렬 currentStreak,desc)")
    fun getMemberStreaks(
        pageable: Pageable,
        @RequestParam(required = false) keyword: String?,
    ): ResponseEntity<PageEnvelope<MemberStreakListItem>> =
        ResponseEntity.ok(memberStreakAdminService.list(pageable, keyword))

    @GetMapping("/summary")
    @Operation(summary = "연속 필사 통계 카드")
    fun getStreakSummary(): ResponseEntity<StreakSummaryResponse> =
        ResponseEntity.ok(memberStreakAdminService.summary())
}
