package store.fillsa.fillsa_api.domain.appVersion.service

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.fixture.appVersion.entity.AppVersionEntityFactory
import store.fillsa.fillsa_api.fixture.appVersion.persist.AppVersionPersistFactory

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppVersionServiceTest @Autowired constructor(
    private val appVersionPersistFactory: AppVersionPersistFactory,
    private val sut: AppVersionService
) {
    
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
} 