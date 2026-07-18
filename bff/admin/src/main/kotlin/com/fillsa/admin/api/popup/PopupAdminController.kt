package com.fillsa.admin.api.popup

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.admin.service.popup.PopupActiveRequest
import com.fillsa.admin.service.popup.PopupAdminService
import com.fillsa.admin.service.popup.PopupResponse
import com.fillsa.admin.service.popup.PopupSaveRequest
import com.fillsa.service.popup.Popup
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/v1/popups")
@Tag(name = "popups", description = "팝업 관리")
class PopupAdminController(
    private val popupAdminService: PopupAdminService,
) {

    @GetMapping
    @Operation(summary = "팝업 목록 (기본 정렬 popupSeq,desc)")
    fun getPopups(
        pageable: Pageable,
        @RequestParam(required = false) popupType: Popup.PopupType?,
        @RequestParam(required = false) isActive: Boolean?,
        @RequestParam(required = false) keyword: String?,
    ): ResponseEntity<PageEnvelope<PopupResponse>> =
        ResponseEntity.ok(popupAdminService.list(pageable, popupType, isActive, keyword))

    @PostMapping
    @Operation(summary = "팝업 등록")
    fun createPopup(@RequestBody request: PopupSaveRequest): ResponseEntity<PopupResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(popupAdminService.create(request))

    @PutMapping("/{popupSeq}")
    @Operation(summary = "팝업 수정")
    fun updatePopup(
        @PathVariable popupSeq: Long,
        @RequestBody request: PopupSaveRequest,
    ): ResponseEntity<PopupResponse> =
        ResponseEntity.ok(popupAdminService.update(popupSeq, request))

    @DeleteMapping("/{popupSeq}")
    @Operation(summary = "팝업 삭제")
    fun deletePopup(@PathVariable popupSeq: Long): ResponseEntity<Void> {
        popupAdminService.delete(popupSeq)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{popupSeq}/active")
    @Operation(summary = "팝업 활성 토글 (목록 스위치용)")
    fun togglePopupActive(
        @PathVariable popupSeq: Long,
        @RequestBody request: PopupActiveRequest,
    ): ResponseEntity<PopupResponse> =
        ResponseEntity.ok(popupAdminService.toggleActive(popupSeq, request.isActive))
}
