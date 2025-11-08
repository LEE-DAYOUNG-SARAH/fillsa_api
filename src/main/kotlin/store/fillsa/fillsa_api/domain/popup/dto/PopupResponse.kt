package store.fillsa.fillsa_api.domain.popup.dto

import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.popup.entity.Popup

data class PopupResponse(
    @Schema(description = "팝업 일련번호", required = true)
    val popupSeq: Long,

    @Schema(description = "팝업 타입", required = true)
    val popupType: Popup.PopupType,

    @Schema(description = "팝업 제목", required = true)
    val title: String,

    @Schema(description = "팝업 이미지 url", required = true)
    val imageUrl: String
) {
    companion object {
        fun from(popup: Popup): PopupResponse {
            return PopupResponse(
                popupSeq = popup.popupSeq,
                popupType = popup.popupType,
                title = popup.title,
                imageUrl = popup.imageUrl
            )
        }
    }
}
