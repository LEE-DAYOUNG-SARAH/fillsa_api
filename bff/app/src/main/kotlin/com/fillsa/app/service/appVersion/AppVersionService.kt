package com.fillsa.app.service.appVersion

import com.fillsa.app.common.cache.TtlCache
import com.fillsa.app.common.cache.evictNowAndAfterCommit
import com.fillsa.service.appversion.AppVersion
import com.fillsa.app.api.appVersion.AppVersionModifyRequest
import com.fillsa.app.api.appVersion.AppVersionResponse
import com.fillsa.service.appversion.AppVersionRepository
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class AppVersionService(
    private val appVersionRepository: AppVersionRepository
) {
    /**
     * 최소 버전 캐시. [verifyAppVersion] 은 인터셉터가 모든 `/api` 요청마다 호출하므로,
     * 캐시가 없으면 요청마다 TiDB 트랜잭션(≈0.5초)이 하나씩 추가된다.
     *
     * ⚠️ 어드민 콘솔(별도 컨테이너)에서 최소 버전을 바꾸면 이 서버에는 최대 [MIN_VERSION_TTL] 뒤에 반영된다.
     */
    private val minVersionCache = TtlCache<String, String>(MIN_VERSION_TTL)

    /**
     * 의도적으로 @Transactional 을 붙이지 않는다 — 붙이면 캐시 적중 시에도 트랜잭션이 열려
     * DB 왕복이 그대로 발생한다. 캐시 미스일 때만 리포지토리(자체 읽기 트랜잭션)가 DB 를 조회한다.
     */
    fun verifyAppVersion(appVersion: String?) {
        if(appVersion.isNullOrBlank()) {
            throw BusinessException(ErrorCode.UNSUPPORTED_APP_VERSION)
        }

        val minVersion = minVersionCache.getOrLoad(MIN_VERSION_KEY) {
            appVersionRepository.findTopByOrderByCreatedAtDesc().minVersion
        }

        if(!isVersionAtLeast(appVersion, minVersion)) {
            throw BusinessException(ErrorCode.UNSUPPORTED_APP_VERSION)
        }
    }

    private fun isVersionAtLeast(request: String, required: String): Boolean {
        val currentParts = request.split(".")
        val requiredParts = required.split(".")
        val maxLength = maxOf(currentParts.size, requiredParts.size)

        for (i in 0 until maxLength) {
            val c = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            val r = requiredParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (c < r) return false
            if (c > r) return true
        }

        return true
    }


    private fun getCurrentAppVersion(): AppVersion = appVersionRepository.findTopByOrderByCreatedAtDesc()

    @Transactional(readOnly = true)
    fun getAppVersion(): AppVersionResponse {
        val appVersion = getCurrentAppVersion()
        return AppVersionResponse.Companion.from(appVersion)
    }

    @Transactional
    fun modify(request: AppVersionModifyRequest): Long {
        val appVersion = getCurrentAppVersion()
        appVersion.modify(request.minVersion, request.nowVersion)
        evictNowAndAfterCommit { evictMinVersionCache() }

        return appVersion.appVersionSeq
    }

    fun evictMinVersionCache() = minVersionCache.clear()

    companion object {
        private const val MIN_VERSION_KEY = "min"
        val MIN_VERSION_TTL: Duration = Duration.ofSeconds(60)
    }
}