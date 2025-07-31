package store.fillsa.fillsa_api.fixture.appVersion.entity

import store.fillsa.fillsa_api.domain.appVersion.domain.AppVersion

class AppVersionEntityFactory {
    companion object {
        fun appVersion(
            minVersion: String = "1.0.0",
            nowVersion: String = "1.1.10"
        ) = AppVersion(
            minVersion = minVersion,
            nowVersion = nowVersion
        )
    }
} 