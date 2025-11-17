package store.fillsa.fillsa_api.domain.popup.dto

import io.swagger.v3.oas.annotations.media.Schema

data class VersionUpdatePopupRequest(
    @Schema(description = "업데이트한 버전")
    val currentVersion: String
)