package store.fillsa.fillsa_api.domain.appVersion.dto

import io.swagger.v3.oas.annotations.media.Schema

data class AppVersionModifyRequest (
    @Schema(description = "최소버전(안보내면 변경안함)")
    val minVersion: String?,

    @Schema(description = "현재버전(안보내면 변경안함)")
    val nowVersion: String?
)