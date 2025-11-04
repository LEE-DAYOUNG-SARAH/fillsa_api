package store.fillsa.fillsa_api.domain.popup.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.domain.popup.dto.PopupResponse
import store.fillsa.fillsa_api.domain.popup.dto.VersionUpdatePopupRequest
import store.fillsa.fillsa_api.domain.popup.service.PopupService

@RestController
@RequestMapping("/api/v1/popups")
class PopupController(
    private val popupService: PopupService
) {
    @Operation(summary = "일반 팝업 조회 API")
    @GetMapping("/general")
    fun generalPopup(): ResponseEntity<PopupResponse?> = ResponseEntity.ok(
        popupService.generalPopup()
    )

    @Operation(summary = "버전 업데이트 팝업 조회 API")
    @GetMapping("/version-update")
    fun versionUpdatePopup(
        request: VersionUpdatePopupRequest
    ): ResponseEntity<PopupResponse?> = ResponseEntity.ok(
        popupService.versionUpdatePopup(request)
    )
}