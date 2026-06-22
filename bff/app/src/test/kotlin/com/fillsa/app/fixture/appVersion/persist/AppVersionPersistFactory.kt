package com.fillsa.app.fixture.appVersion.persist

import org.springframework.stereotype.Component
import com.fillsa.service.appversion.AppVersion
import com.fillsa.service.appversion.AppVersionRepository
import com.fillsa.app.fixture.appVersion.entity.AppVersionEntityFactory

@Component
class AppVersionPersistFactory(
    private val appVersionRepository: AppVersionRepository
) {
    fun createAppVersion(appVersion: AppVersion = AppVersionEntityFactory.appVersion()): AppVersion {
        return appVersionRepository.save(appVersion)
    }
} 