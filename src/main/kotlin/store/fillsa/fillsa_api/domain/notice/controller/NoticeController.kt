package store.fillsa.fillsa_api.domain.notice.controller

import store.fillsa.fillsa_api.common.dto.PageResponse
import store.fillsa.fillsa_api.domain.notice.dto.NoticeResponse
import store.fillsa.fillsa_api.domain.notice.service.useCase.NoticeUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notices")
@Tag(name = "공지사항", description = "공지사항 조회 api")
class NoticeController(
    private val noticeUseCase: NoticeUseCase
) {
    @GetMapping
    @Operation(summary = "공지사항 목록 조회 api")
    fun notices(
        pageable: Pageable
    ): ResponseEntity<PageResponse<NoticeResponse>> = ResponseEntity.ok(
        noticeUseCase.getNotices(pageable)
    )
}