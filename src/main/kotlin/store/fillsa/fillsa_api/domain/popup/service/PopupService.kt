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
        val popup = popupRepository.findLatestActiveVersionUpdatePopup(LocalDateTime.now())


        return popup?.let {
            val targetVersion = it.targetVersion ?: return null
            if (compareVersion(targetVersion, request.previousVersion) > 0 &&
                compareVersion(targetVersion, request.currentVersion) <= 0) {
                PopupResponse.from(it)
            } else {
                null
            }
        }
    }

    private fun compareVersion(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(v1Parts.size, v2Parts.size)

        for (i in 0 until maxLength) {
            val v1Part = v1Parts.getOrElse(i) { 0 }
            val v2Part = v2Parts.getOrElse(i) { 0 }

            if (v1Part != v2Part) {
                return v1Part.compareTo(v2Part)
            }
        }

        return 0
    }
}