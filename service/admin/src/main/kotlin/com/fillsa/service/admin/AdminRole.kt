package com.fillsa.service.admin

/**
 * 어드민 권한 등급. Spring Security 에서는 "ROLE_" 접두사를 붙여 사용합니다(ROLE_SUPER 등).
 */
enum class AdminRole {
    /** 최고 관리자 — 어드민 계정 관리 포함 전권 */
    SUPER,

    /** 일반 관리자 — 콘텐츠 등록/수정 */
    ADMIN,

    /** 운영자 — 조회 위주 */
    OPERATOR,
    ;

    fun authority(): String = "ROLE_$name"
}
