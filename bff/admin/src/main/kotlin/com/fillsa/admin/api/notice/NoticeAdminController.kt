package com.fillsa.admin.api.notice

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.admin.service.notice.NoticeAdminService
import com.fillsa.admin.service.notice.NoticeResponse
import com.fillsa.admin.service.notice.NoticeSaveRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/v1/notices")
@Tag(name = "notices", description = "공지 관리")
class NoticeAdminController(
    private val noticeAdminService: NoticeAdminService,
) {

    @GetMapping
    @Operation(summary = "공지 목록 (기본 정렬 noticeSeq,desc)")
    fun getNotices(
        pageable: Pageable,
        @RequestParam(required = false) keyword: String?,
    ): ResponseEntity<PageEnvelope<NoticeResponse>> =
        ResponseEntity.ok(noticeAdminService.list(pageable, keyword))

    @PostMapping
    @Operation(summary = "공지 작성")
    fun createNotice(@RequestBody request: NoticeSaveRequest): ResponseEntity<NoticeResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(noticeAdminService.create(request))

    @PutMapping("/{noticeSeq}")
    @Operation(summary = "공지 수정")
    fun updateNotice(
        @PathVariable noticeSeq: Long,
        @RequestBody request: NoticeSaveRequest,
    ): ResponseEntity<NoticeResponse> =
        ResponseEntity.ok(noticeAdminService.update(noticeSeq, request))

    @DeleteMapping("/{noticeSeq}")
    @Operation(summary = "공지 삭제")
    fun deleteNotice(@PathVariable noticeSeq: Long): ResponseEntity<Void> {
        noticeAdminService.delete(noticeSeq)
        return ResponseEntity.noContent().build()
    }
}
