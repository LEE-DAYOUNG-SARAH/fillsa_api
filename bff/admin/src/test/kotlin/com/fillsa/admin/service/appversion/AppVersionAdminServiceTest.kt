package com.fillsa.admin.service.appversion

import com.fillsa.admin.fixture.AdminFixtures
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppVersionAdminServiceTest @Autowired constructor(
    private val sut: AppVersionAdminService,
    private val adminFixtures: AdminFixtures,
) {

    @Test
    fun `조회 - 존재하는 앱 버전 정보를 반환한다`() {
        adminFixtures.appVersion(minVersion = "1.0.0", nowVersion = "1.2.0")

        val result = sut.get()

        assertThat(result.minVersion).isEqualTo("1.0.0")
        assertThat(result.nowVersion).isEqualTo("1.2.0")
    }

    @Test
    fun `조회 - 데이터가 없으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.get() }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `수정 - minVersion, nowVersion 을 갱신한다`() {
        adminFixtures.appVersion(minVersion = "1.0.0", nowVersion = "1.0.0")

        val result = sut.update(AppVersionModifyRequest(minVersion = "1.1.0", nowVersion = "1.2.0"))

        assertThat(result.minVersion).isEqualTo("1.1.0")
        assertThat(result.nowVersion).isEqualTo("1.2.0")
    }

    @Test
    fun `수정 - blank 또는 null 값은 무시된다`() {
        adminFixtures.appVersion(minVersion = "1.0.0", nowVersion = "1.0.0")

        val result = sut.update(AppVersionModifyRequest(minVersion = "", nowVersion = null))

        assertThat(result.minVersion).isEqualTo("1.0.0")
        assertThat(result.nowVersion).isEqualTo("1.0.0")
    }

    @Test
    fun `수정 - 데이터가 없으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.update(AppVersionModifyRequest(minVersion = "1.1.0")) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
}
