package com.fillsa.service.appversion

import org.springframework.data.jpa.repository.JpaRepository

interface AppVersionRepository: JpaRepository<AppVersion, Long> {
    fun findTopByOrderByCreatedAtDesc(): AppVersion
}