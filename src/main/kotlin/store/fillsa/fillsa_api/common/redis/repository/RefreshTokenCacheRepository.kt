package store.fillsa.fillsa_api.common.redis.repository

import org.springframework.data.repository.CrudRepository
import store.fillsa.fillsa_api.common.redis.entity.RefreshTokenCache

interface RefreshTokenCacheRepository : CrudRepository<RefreshTokenCache, String> {
    fun findByMemberId(memberId: Long): List<RefreshTokenCache>
    fun deleteByMemberId(memberId: Long)
}