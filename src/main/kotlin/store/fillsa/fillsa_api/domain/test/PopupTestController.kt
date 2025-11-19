package store.fillsa.fillsa_api.domain.test

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.domain.popup.dto.PopupResponse
import store.fillsa.fillsa_api.domain.popup.entity.Popup

@RestController
@RequestMapping("/api/test/popups")
class PopupTestController {

    // TODO. 위젯 운영 배포 후 제거
    @GetMapping("/event")
    @Operation(summary = "[개발자용] 이벤트 팝업 조회 API 테스트")
    fun testPopup(): ResponseEntity<PopupResponse> = ResponseEntity.ok(
        PopupResponse(
            popupSeq = 10,
            popupType = Popup.PopupType.EVENT,
            title = "크리스마스 이벤트",
            content = "2025 크리스마스 이벤트에 참여해보세요. \r\n 자세한 사항은 공지사항을 확인해주세요.",
            imageUrl = null
        )
    )
}