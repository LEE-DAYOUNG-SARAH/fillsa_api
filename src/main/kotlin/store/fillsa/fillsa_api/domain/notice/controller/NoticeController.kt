package store.fillsa.fillsa_api.domain.notice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.common.dto.PageResponse
import store.fillsa.fillsa_api.domain.notice.dto.NoticeResponse
import store.fillsa.fillsa_api.domain.notice.service.NoticeService

@RestController
@RequestMapping("/notices")
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