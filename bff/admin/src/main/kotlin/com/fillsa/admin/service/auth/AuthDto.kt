package com.fillsa.admin.service.auth

import com.fillsa.service.admin.AdminEntity
import com.fillsa.service.admin.AdminRole
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class LoginRequest(
    @field:NotBlank
    val loginId: String,
    @field:NotBlank
    val password: String,
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class AdminProfile(
    val adminSeq: Long,
    val loginId: String,
    val name: String,
    val role: AdminRole,
    val lastLoginAt: LocalDateTime?,
) {
    companion object {
        fun from(admin: AdminEntity) = AdminProfile(
            adminSeq = admin.adminSeq,
            loginId = admin.loginId,
            name = admin.name,
            role = admin.role,
            lastLoginAt = admin.lastLoginAt,
        )
    }
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val admin: AdminProfile,
)
