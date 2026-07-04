package com.fillsa.admin.api.member

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.admin.service.member.MemberAdminRoleRequest
import com.fillsa.admin.service.member.MemberAdminService
import com.fillsa.admin.service.member.MemberDetailResponse
import com.fillsa.admin.service.member.MemberListItem
import com.fillsa.admin.service.member.MemberSummaryResponse
import com.fillsa.service.member.Member
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/v1/members")
@Tag(name = "members", description = "회원 관리")
class MemberAdminController(
    private val memberAdminService: MemberAdminService,
) {

    @GetMapping
    @Operation(summary = "회원 목록 (기본 정렬 createdAt,desc)")
    fun getMembers(
        pageable: Pageable,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) oauthProvider: Member.OAuthProvider?,
        @RequestParam(required = false) withdrawalYn: String?,
    ): ResponseEntity<PageEnvelope<MemberListItem>> =
        ResponseEntity.ok(memberAdminService.list(pageable, keyword, oauthProvider, withdrawalYn))

    @GetMapping("/summary")
    @Operation(summary = "회원 통계 카드 (전체/활성/탈퇴/팀원)")
    fun getMemberSummary(): ResponseEntity<MemberSummaryResponse> =
        ResponseEntity.ok(memberAdminService.summary())

    @GetMapping("/{memberSeq}")
    @Operation(summary = "회원 상세 (스트릭·누적 필사·디바이스 포함)")
    fun getMember(@PathVariable memberSeq: Long): ResponseEntity<MemberDetailResponse> =
        ResponseEntity.ok(memberAdminService.detail(memberSeq))

    @PutMapping("/{memberSeq}/admin-role")
    @Operation(summary = "팀원(관리자) 표시 지정/해제 (members.adminYn 토글)")
    fun updateMemberAdminRole(
        @PathVariable memberSeq: Long,
        @RequestBody request: MemberAdminRoleRequest,
    ): ResponseEntity<MemberListItem> =
        ResponseEntity.ok(memberAdminService.toggleAdminRole(memberSeq, request.adminYn))
}
