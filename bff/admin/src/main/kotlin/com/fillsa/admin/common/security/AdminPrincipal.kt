package com.fillsa.admin.common.security

import com.fillsa.service.admin.AdminRole

/**
 * SecurityContext 에 저장되는 인증 주체. 컨트롤러에서 @AuthenticationPrincipal 로 주입받는다.
 */
data class AdminPrincipal(
    val adminSeq: Long,
    val role: AdminRole,
)
