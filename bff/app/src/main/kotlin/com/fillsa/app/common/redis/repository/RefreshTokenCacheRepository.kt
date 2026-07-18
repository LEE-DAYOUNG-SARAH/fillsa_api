package com.fillsa.app.common.redis.repository

import org.springframework.data.repository.CrudRepository
import com.fillsa.app.common.redis.entity.RefreshTokenCache

interface RefreshTokenCacheRepository : CrudRepository<RefreshTokenCache, String> {
    fun findByMemberId(memberId: Long): List<RefreshTokenCache>
    fun deleteByMemberId(memberId: Long)
}