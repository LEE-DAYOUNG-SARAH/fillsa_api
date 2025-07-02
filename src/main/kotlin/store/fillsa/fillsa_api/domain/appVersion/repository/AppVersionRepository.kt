package store.fillsa.fillsa_api.domain.appVersion.repository

import org.springframework.data.jpa.repository.JpaRepository
import store.fillsa.fillsa_api.domain.appVersion.domain.AppVersion

interface AppVersionRepository: JpaRepository<AppVersion, Long> {
    fun findTopByOrderByCreatedAtDesc(): AppVersion
}