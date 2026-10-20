package com.fillsa.app.common.security

import com.fillsa.app.fixture.member.entity.MemberEntityFactory
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MemberAuthCacheTest {

    private val sut = MemberAuthCache()

    @Test
    fun `같은 회원은 두 번째 요청부터 DB 를 조회하지 않는다`() {
        var calls = 0
        val loader: (Long) -> com.fillsa.service.member.Member = { calls++; MemberEntityFactory.member() }

        sut.getOrLoad(1L, loader)
        sut.getOrLoad(1L, loader)

        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun `요청마다 다른 사본을 돌려주되 값은 같다`() {
        val loaded = MemberEntityFactory.member(nickname = "다영")

        val first = sut.getOrLoad(1L) { loaded }
        val second = sut.getOrLoad(1L) { error("호출되면 안 됨") }

        assertThat(first).isNotSameAs(second).isNotSameAs(loaded)
        assertThat(second.nickname).isEqualTo("다영")
        assertThat(second.memberSeq).isEqualTo(loaded.memberSeq)
        assertThat(second.createdAt).isEqualTo(loaded.createdAt)
    }

    @Test
    fun `evict 후에는 다시 조회한다`() {
        var calls = 0
        sut.getOrLoad(1L) { calls++; MemberEntityFactory.member() }

        sut.evict(1L)
        sut.getOrLoad(1L) { calls++; MemberEntityFactory.member() }

        assertThat(calls).isEqualTo(2)
    }

    @Test
    fun `로더가 탈퇴 예외를 던지면 캐시하지 않고 그대로 전파한다`() {
        assertThatThrownBy {
            sut.getOrLoad(1L) { throw BusinessException(ErrorCode.WITHDRAWAL_USER) }
        }.isInstanceOf(BusinessException::class.java)

        var calls = 0
        sut.getOrLoad(1L) { calls++; MemberEntityFactory.member() }
        assertThat(calls).isEqualTo(1)
    }
}
