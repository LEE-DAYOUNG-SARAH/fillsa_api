package store.fillsa.fillsa_api.domain.appVersion.service

import org.springframework.stereotype.Service
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.domain.appVersion.repository.AppVersionRepository

@Service
class AppVersionService(
    private val appVersionRepository: AppVersionRepository
) {
    fun verifyAppVersion(appVersion: String?) {
        if(appVersion.isNullOrBlank()) {
            throw BusinessException(ErrorCode.UNSUPPORTED_APP_VERSION)
        }

        val minVersion = appVersionRepository.findTopByOrderByCreatedAtDesc()
            .minVersion

        if(!isVersionAtLeast(appVersion, minVersion)) {
            throw BusinessException(ErrorCode.UNSUPPORTED_APP_VERSION)
        }
    }

    private fun isVersionAtLeast(current: String, required: String): Boolean {
        val currentParts = current.split(".")
        val requiredParts = required.split(".")
        val maxLength = maxOf(currentParts.size, requiredParts.size)

        for (i in 0 until maxLength) {
            val c = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            val r = requiredParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (c < r) return false
            if (c > r) return true
        }

        return true
    }
}