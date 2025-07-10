package store.fillsa.fillsa_api.fixture.appVersion.persist

import org.springframework.stereotype.Component
import store.fillsa.fillsa_api.domain.appVersion.domain.AppVersion
import store.fillsa.fillsa_api.domain.appVersion.repository.AppVersionRepository
import store.fillsa.fillsa_api.fixture.appVersion.entity.AppVersionEntityFactory

@Component
class AppVersionPersistFactory(
    private val appVersionRepository: AppVersionRepository
) {
    fun createAppVersion(appVersion: AppVersion = AppVersionEntityFactory.appVersion()): AppVersion {
        return appVersionRepository.save(appVersion)
    }
} 