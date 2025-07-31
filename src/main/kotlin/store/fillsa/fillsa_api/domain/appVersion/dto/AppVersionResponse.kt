package store.fillsa.fillsa_api.domain.appVersion.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.appVersion.domain.AppVersion
import java.time.LocalDateTime

data class AppVersionResponse(
    @Schema(description = "최소버전")
    val minVersion: String?,

    @Schema(description = "현재버전")
    val nowVersion: String?,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss", timezone = "Asia/Seoul")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(appVersion: AppVersion) = AppVersionResponse(
            minVersion = appVersion.minVersion,
            nowVersion = appVersion.nowVersion,
            updatedAt = appVersion.updatedAt
        )
    }
}