package store.fillsa.fillsa_api.domain.appVersion.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        val exception = assertThrows<BusinessException> {
            sut.verifyAppVersion(appVersion)
        }
        
        assertThat(exception.errorCode).isEqualTo(ErrorCode.UNSUPPORTED_APP_VERSION)
    }
    
    @Test
    fun `앱 버전 검증 성공 - 버전이 null인 경우 현재는 통과한다`() {
        // given
        val appVersion: String? = null
        
        // when & then
        assertThatCode { sut.verifyAppVersion(appVersion) }.doesNotThrowAnyException()
    }
    
    @Test
    fun `앱 버전 검증 성공 - 버전이 빈 문자열인 경우 현재는 통과한다`() {
        // given
        val appVersion = ""
        
        // when & then
        assertThatCode { sut.verifyAppVersion(appVersion) }.doesNotThrowAnyException()
    }
    
    @Test
    fun `앱 버전 검증 성공 - 버전이 공백인 경우 현재는 통과한다`() {
        // given
        val appVersion = "   "
        
        // when & then
        assertThatCode { sut.verifyAppVersion(appVersion) }.doesNotThrowAnyException()
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