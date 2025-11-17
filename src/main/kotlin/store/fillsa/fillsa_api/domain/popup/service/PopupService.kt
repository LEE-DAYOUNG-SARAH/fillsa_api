package store.fillsa.fillsa_api.domain.popup.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.popup.dto.PopupResponse
import store.fillsa.fillsa_api.domain.popup.dto.VersionUpdatePopupRequest
import store.fillsa.fillsa_api.domain.popup.repository.PopupRepository
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