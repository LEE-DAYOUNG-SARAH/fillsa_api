package com.fillsa.admin.service.popup

import com.fillsa.service.popup.Popup
import java.time.LocalDateTime

data class PopupSaveRequest(
    val popupType: Popup.PopupType,
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val isActive: Boolean,
    val targetVersion: String? = null,
)

data class PopupActiveRequest(
    val isActive: Boolean,
)

data class PopupResponse(
    val popupSeq: Long,
    val popupType: Popup.PopupType,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val isActive: Boolean,
    val targetVersion: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(popup: Popup) = PopupResponse(
            popupSeq = popup.popupSeq,
            popupType = popup.popupType,
            title = popup.title,
            content = popup.content,
            imageUrl = popup.imageUrl,
            startDateTime = popup.startDateTime,
            endDateTime = popup.endDateTime,
            isActive = popup.isActive,
            targetVersion = popup.targetVersion,
            createdAt = popup.createdAt,
        )
    }
}
