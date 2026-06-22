package com.fillsa.app.service.popup

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.api.popup.PopupResponse
import com.fillsa.app.api.popup.VersionUpdatePopupRequest
import com.fillsa.service.popup.PopupRepository
import java.time.LocalDateTime

@Service
class PopupService(
    private val popupRepository: PopupRepository
) {
    @Transactional(readOnly = true)
    fun generalPopup(): PopupResponse? {
        val generalPopup = popupRepository.findActiveGeneralPopup(LocalDateTime.now())

        return generalPopup?.let {
            PopupResponse.from(it)
        }
    }

    @Transactional(readOnly = true)
    fun versionUpdatePopup(request: VersionUpdatePopupRequest): PopupResponse? {
        val versionUpdatePopup =
            popupRepository.findLatestActiveVersionUpdatePopup(LocalDateTime.now(), request.currentVersion)

        return versionUpdatePopup?.let {
            PopupResponse.from(it)
        }
    }
}