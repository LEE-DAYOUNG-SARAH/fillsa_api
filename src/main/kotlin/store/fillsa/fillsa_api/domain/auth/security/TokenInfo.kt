package store.fillsa.fillsa_api.domain.auth.security

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)