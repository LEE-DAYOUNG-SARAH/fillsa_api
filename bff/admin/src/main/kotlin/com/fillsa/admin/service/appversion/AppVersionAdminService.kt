package com.fillsa.admin.service.appversion

import com.fillsa.service.appversion.AppVersion
import com.fillsa.service.appversion.AppVersionRepository
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppVersionAdminService(
    private val appVersionRepository: AppVersionRepository,
) {

    @Transactional(readOnly = true)
    fun get(): AppVersionResponse = AppVersionResponse.from(getCurrentAppVersion())

    @Transactional
    fun update(request: AppVersionModifyRequest): AppVersionResponse {
        val appVersion = getCurrentAppVersion()
        appVersion.modify(request.minVersion, request.nowVersion)
        return AppVersionResponse.from(appVersion)
    }

    private fun getCurrentAppVersion(): AppVersion =
        appVersionRepository
            .findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")))
            .content
            .firstOrNull()
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "앱 버전 정보가 존재하지 않습니다.")
}
