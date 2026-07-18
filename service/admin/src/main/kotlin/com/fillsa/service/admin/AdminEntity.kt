package com.fillsa.service.admin

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 어드민 계정. 앱 회원(members)과 완전히 분리된 별도 테이블(admins).
 * 어드민 로그인은 이 테이블의 loginId/password 로 인증합니다.
 */
@Entity
@Table(
    name = "admins",
    uniqueConstraints = [UniqueConstraint(name = "uk_admins_login_id", columnNames = ["login_id"])],
)
class AdminEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_seq")
    val adminSeq: Long = 0L,

    @Column(name = "login_id", nullable = false, length = 100)
    val loginId: String,

    /** BCrypt 등으로 해시된 비밀번호. 평문 저장 금지. */
    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "name", nullable = false, length = 50)
    var name: String,

    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var role: AdminRole = AdminRole.ADMIN,

    @Column(name = "active_yn", nullable = false, columnDefinition = "char(1)")
    var activeYn: String = "Y",

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun isActive(): Boolean = activeYn == "Y"

    fun changePassword(encodedPassword: String) {
        this.password = encodedPassword
        this.updatedAt = LocalDateTime.now()
    }

    fun recordLogin() {
        this.lastLoginAt = LocalDateTime.now()
    }
}
