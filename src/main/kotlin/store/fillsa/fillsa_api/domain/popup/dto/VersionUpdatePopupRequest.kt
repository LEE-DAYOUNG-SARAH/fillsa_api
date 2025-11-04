package store.fillsa.fillsa_api.domain.popup.dto

data class VersionUpdatePopupRequest(
    val currentVersion: String,
    val previousVersion: String,
)