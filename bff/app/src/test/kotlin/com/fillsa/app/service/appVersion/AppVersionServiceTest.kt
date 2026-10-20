package com.fillsa.app.service.appVersion

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import com.fillsa.app.fixture.appVersion.entity.AppVersionEntityFactory
import com.fillsa.app.fixture.appVersion.persist.AppVersionPersistFactory
import com.fillsa.app.service.appVersion.AppVersionService
import com.fillsa.app.api.appVersion.AppVersionModifyRequest

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppVersionServiceTest @Autowired constructor(
    private val appVersionPersistFactory: AppVersionPersistFactory,
    private val sut: AppVersionService
) {

    // 최소 버전은 서비스 내부에 캐시된다(TTL 60초). 테스트 간 값이 새지 않도록 매번 비운다.
    @BeforeEach
    fun setUp() {
        sut.evictMinVersionCache()
    }

    @Test
    fun `앱 버전 검증 성공 - 지원하는 버전인 경우 통과한다`() {
        // given
        val appVersion = "1.5.0"
        appVersionPersistFactory.createAppVersion(
            AppVersionEntityFactory.appVersion(minVersion = "1.0.0")
        )
        
        // when & then
        assertThatCode { sut.verifyAppVersion(appVersion) }.doesNotThrowAnyException()
    }
    
    @Test
    fun `앱 버전 검증 성공 - 최소 버전과 동일한 경우 통과한다`() {
        // given
        val appVersion = "1.0.0"
        appVersionPersistFactory.createAppVersion(
            AppVersionEntityFactory.appVersion(minVersion = "1.0.0")
        )
        
        // when & then
        assertThatCode { sut.verifyAppVersion(appVersion) }.doesNotThrowAnyException()
    }
    
    @Test
    fun `앱 버전 검증 성공 - 더 높은 버전인 경우 통과한다`() {
        // given
        val appVersion = "2.0.0"
        appVersionPersistFactory.createAppVersion(
            AppVersionEntityFactory.appVersion(minVersion = "1.0.0")
        )
        
        // when & then
        assertThatCode { sut.verifyAppVersion(appVersion) }.doesNotThrowAnyException()
    }
    
    @Test
    fun `앱 버전 검증 실패 - 지원하지 않는 낮은 버전인 경우 예외를 던진다`() {
        // given
        val appVersion = "0.9.0"
        appVersionPersistFactory.createAppVersion(
            AppVersionEntityFactory.appVersion(minVersion = "1.0.0")
        )
        
        // when & then
        assertThatThrownBy { sut.verifyAppVersion(appVersion) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_APP_VERSION)
    }
    
    @Test
    fun `앱 버전 검증 성공 - 자릿수가 다른 버전도 정상 비교한다`() {
        // given
        val appVersion = "1.2"
        appVersionPersistFactory.createAppVersion(
            AppVersionEntityFactory.appVersion(minVersion = "1.2.0")
        )
        
        // when & then
        assertThatCode { sut.verifyAppVersion(appVersion) }.doesNotThrowAnyException()
    }

    @Test
    fun `최소 버전은 캐시되어 DB 값이 바뀌어도 TTL 동안 재조회하지 않는다`() {
        // given — 최소 1.0.0 으로 한 번 검증해 캐시를 채운다
        val appVersion = appVersionPersistFactory.createAppVersion(
            AppVersionEntityFactory.appVersion(minVersion = "1.0.0")
        )
        sut.verifyAppVersion("1.0.0")

        // when — 캐시를 거치지 않는 경로(엔티티 직접 수정)로 최소 버전을 올린다
        appVersion.modify(minVersion = "2.0.0", nowVersion = null)

        // then — 캐시된 1.0.0 기준으로 판정된다
        assertThatCode { sut.verifyAppVersion("1.0.0") }.doesNotThrowAnyException()
    }

    @Test
    fun `앱 서버의 버전 수정 API 는 캐시를 비워 즉시 반영된다`() {
        // given
        appVersionPersistFactory.createAppVersion(
            AppVersionEntityFactory.appVersion(minVersion = "1.0.0")
        )
        sut.verifyAppVersion("1.0.0")

        // when
        sut.modify(AppVersionModifyRequest(minVersion = "2.0.0", nowVersion = null))

        // then
        assertThatThrownBy { sut.verifyAppVersion("1.0.0") }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_APP_VERSION)
    }
}
