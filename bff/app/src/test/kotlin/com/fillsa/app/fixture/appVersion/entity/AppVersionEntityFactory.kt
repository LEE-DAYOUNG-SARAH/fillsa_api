package com.fillsa.app.fixture.appVersion.entity

import com.fillsa.service.appversion.AppVersion

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