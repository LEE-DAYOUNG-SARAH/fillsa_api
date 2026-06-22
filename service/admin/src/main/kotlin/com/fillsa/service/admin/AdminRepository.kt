package com.fillsa.service.admin

import org.springframework.data.jpa.repository.JpaRepository

interface AdminRepository : JpaRepository<AdminEntity, Long> {
    fun findByLoginId(loginId: String): AdminEntity?
    fun existsByLoginId(loginId: String): Boolean
}
