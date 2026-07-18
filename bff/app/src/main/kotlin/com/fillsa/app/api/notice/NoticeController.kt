package com.fillsa.app.api.notice

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.fillsa.app.common.dto.PageResponse
import com.fillsa.app.api.notice.NoticeResponse
import com.fillsa.app.service.notice.NoticeService

@RestController
@RequestMapping("/api/v1/notices")
@Tag(name = "공지사항")
class NoticeController(
    private val noticeService: NoticeService
) {
    @GetMapping
    @Operation(summary = "[2-3. write] 공지사항 목록 조회 api")
    fun notices(
        pageable: Pageable
    ): ResponseEntity<PageResponse<NoticeResponse>> = ResponseEntity.ok(
        noticeService.getNotices(pageable)
    )
}