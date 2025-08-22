package store.fillsa.fillsa_api.domain.appVersion.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.domain.appVersion.domain.AppVersion
import store.fillsa.fillsa_api.domain.appVersion.dto.AppVersionModifyRequest
import store.fillsa.fillsa_api.domain.appVersion.dto.AppVersionResponse
import store.fillsa.fillsa_api.domain.appVersion.repository.AppVersionRepository

@Service
class AppVersionService(
    private val appVersionRepository: AppVersionRepository
) {
    @Transactional(readOnly = true)
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

    private fun isVersionAtLeast(request: String, required: String): Boolean {
        val currentParts = request.split(".")
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


    private fun getCurrentAppVersion(): AppVersion = appVersionRepository.findTopByOrderByCreatedAtDesc()

    @Transactional(readOnly = true)
    fun getAppVersion(): AppVersionResponse {
        val appVersion = getCurrentAppVersion()
        return AppVersionResponse.from(appVersion)
    }

    @Transactional
    fun modify(request: AppVersionModifyRequest): Long {
        val appVersion = getCurrentAppVersion()
        appVersion.modify(request.minVersion, request.nowVersion)

        return appVersion.appVersionSeq
    }
}