package com.fillsa.admin.service.auth

import com.fillsa.admin.common.security.AdminJwtTokenProvider
import com.fillsa.service.admin.AdminEntity
import com.fillsa.service.admin.AdminRepository
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminAuthService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val adminJwtTokenProvider: AdminJwtTokenProvider,
) {

    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val admin = adminRepository.findByLoginId(request.loginId)
            ?: throw BusinessException(ErrorCode.ADMIN_LOGIN_FAILED)

        if (!passwordEncoder.matches(request.password, admin.password)) {
            throw BusinessException(ErrorCode.ADMIN_LOGIN_FAILED)
        }

        if (!admin.isActive()) {
            throw BusinessException(ErrorCode.ADMIN_ACCOUNT_INACTIVE)
        }

        admin.recordLogin()

        return issueTokens(admin)
    }

    /**
     * 무상태(stateless) 재발급. 앱(bff:app)의 Redis 기반 리프레시 저장소는 어드민에 도입하지 않고,
     * 리프레시 토큰의 서명/만료/tokenType 검증만으로 재발급한다(계약 준수).
     */
    @Transactional(readOnly = true)
    fun refresh(request: RefreshRequest): TokenResponse {
        val claims = try {
            adminJwtTokenProvider.parseClaims(request.refreshToken)
        } catch (e: ExpiredJwtException) {
            throw BusinessException(ErrorCode.ADMIN_TOKEN_EXPIRED)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.ADMIN_TOKEN_INVALID)
        }

        if (!adminJwtTokenProvider.isAdminToken(claims) || !adminJwtTokenProvider.isRefreshToken(claims)) {
            throw BusinessException(ErrorCode.ADMIN_TOKEN_INVALID)
        }

        val adminSeq = adminJwtTokenProvider.getAdminSeq(claims)
        val admin = adminRepository.findById(adminSeq)
            .orElseThrow { BusinessException(ErrorCode.ADMIN_TOKEN_INVALID) }

        if (!admin.isActive()) {
            throw BusinessException(ErrorCode.ADMIN_ACCOUNT_INACTIVE)
        }

        return issueTokens(admin)
    }

    private fun issueTokens(admin: AdminEntity): TokenResponse {
        val accessToken = adminJwtTokenProvider.createAccessToken(admin.adminSeq, admin.role)
        val refreshToken = adminJwtTokenProvider.createRefreshToken(admin.adminSeq, admin.role)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = adminJwtTokenProvider.accessTokenValidity / 1000,
            admin = AdminProfile.from(admin),
        )
    }
}
