package com.fillsa.admin.service.appversion

import com.fillsa.service.appversion.AppVersion
import java.time.LocalDateTime

data class AppVersionModifyRequest(
    val minVersion: String? = null,
    val nowVersion: String? = null,
)

data class AppVersionResponse(
    val minVersion: String,
    val nowVersion: String,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(appVersion: AppVersion) = AppVersionResponse(
            minVersion = appVersion.minVersion,
            nowVersion = appVersion.nowVersion,
            updatedAt = appVersion.updatedAt,
        )
    }
}
