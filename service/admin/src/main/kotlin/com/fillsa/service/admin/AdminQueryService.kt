package com.fillsa.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 어드민 계정 조회. 인증(로그인) 시 bff:admin 에서 호출합니다.
 */
@Service
@Transactional(readOnly = true)
class AdminQueryService(
    private val adminRepository: AdminRepository,
) {
    fun findByLoginId(loginId: String): AdminEntity? =
        adminRepository.findByLoginId(loginId)

    fun getActiveByLoginId(loginId: String): AdminEntity? =
        adminRepository.findByLoginId(loginId)?.takeIf { it.isActive() }
}
