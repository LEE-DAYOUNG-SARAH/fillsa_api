package com.fillsa.app.api.popup

import io.swagger.v3.oas.annotations.media.Schema

data class VersionUpdatePopupRequest(
    @Schema(description = "업데이트한 버전")
    val currentVersion: String
)