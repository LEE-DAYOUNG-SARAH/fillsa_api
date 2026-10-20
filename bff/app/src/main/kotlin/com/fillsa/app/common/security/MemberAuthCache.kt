package com.fillsa.app.common.security

import com.fillsa.app.common.cache.TtlCache
import com.fillsa.app.common.cache.evictNowAndAfterCommit
import com.fillsa.service.member.Member
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * JWT 인증 필터용 회원 캐시. 탈퇴하지 않은 회원만 담는다.
 *
 * 필터는 로그인 요청마다 회원을 조회하는데, 캐시가 없으면 요청마다 TiDB 트랜잭션(≈0.5초)이 하나씩 추가된다.
 *
 * 무효화
 * - 탈퇴: [com.fillsa.app.service.members.member.MemberService.withdraw] 가 커밋 직후 지운다 → 탈퇴 토큰은 즉시 차단
 * - 로그인 시 닉네임·프로필 갱신: signUp 이 커밋 직후 지운다
 * - 그 밖의 변경(어드민 콘솔의 adminYn 토글 등, 별도 컨테이너)은 최대 [TTL] 뒤 반영된다.
 *   앱 서버는 adminYn 을 사용하지 않는다.
 *
 * 캐시된 인스턴스를 그대로 내주지 않고 요청마다 사본을 만든다.
 * 여러 요청 스레드가 같은 엔티티 객체를 공유하지 않게 하기 위함이다.
 * 사본은 기존과 동일한 detached 엔티티(식별자 보유)라 FK 참조 등 기존 사용 방식과 같다.
 */
@Component
class MemberAuthCache {
    private val cache = TtlCache<Long, Member>(TTL)

    fun getOrLoad(memberSeq: Long, loader: (Long) -> Member): Member {
        val cached = cache.get(memberSeq) ?: loader(memberSeq).also { loaded ->
            if (!loaded.isWithdrawal()) cache.put(memberSeq, copyOf(loaded))
        }
        return copyOf(cached)
    }

    /** 현재 트랜잭션이 있으면 커밋 직후에도 한 번 더 지운다. */
    fun evict(memberSeq: Long) = evictNowAndAfterCommit { cache.evict(memberSeq) }

    fun clear() = cache.clear()

    private fun copyOf(member: Member): Member =
        Member(
            memberSeq = member.memberSeq,
            oauthProvider = member.oauthProvider,
            oauthId = member.oauthId,
            nickname = member.nickname,
            profileImageUrl = member.profileImageUrl,
            withdrawalYn = member.withdrawalYn,
            withdrawalAt = member.withdrawalAt,
            adminYn = member.adminYn,
        ).also {
            it.createdAt = member.createdAt
            it.updatedAt = member.updatedAt
        }

    companion object {
        val TTL: Duration = Duration.ofMinutes(5)
    }
}
