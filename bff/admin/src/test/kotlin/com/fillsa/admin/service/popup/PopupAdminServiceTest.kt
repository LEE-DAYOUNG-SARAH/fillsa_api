package com.fillsa.admin.service.popup

import com.fillsa.admin.fixture.AdminFixtures
import com.fillsa.service.popup.Popup
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PopupAdminServiceTest @Autowired constructor(
    private val sut: PopupAdminService,
    private val adminFixtures: AdminFixtures,
    private val popupAdminRepository: PopupAdminQueryRepository,
) {

    @Test
    fun `목록 - 기본 정렬 popupSeq desc`() {
        val p1 = adminFixtures.popup(title = "첫번째 팝업")
        val p2 = adminFixtures.popup(title = "두번째 팝업")

        val page = sut.list(PageRequest.of(0, 10), null, null, null)

        assertThat(page.content).hasSize(2)
        // 기본 정렬 popupSeq desc → 나중에 저장한 p2 가 먼저
        assertThat(page.content[0].popupSeq).isEqualTo(p2.popupSeq)
        assertThat(page.content[1].popupSeq).isEqualTo(p1.popupSeq)
        assertThat(page.page.totalElements).isEqualTo(2)
    }

    @Test
    fun `목록 - popupType 필터`() {
        adminFixtures.popup(title = "공지형", popupType = Popup.PopupType.NOTICE)
        adminFixtures.popup(title = "이벤트형", popupType = Popup.PopupType.EVENT)

        val page = sut.list(PageRequest.of(0, 10), Popup.PopupType.EVENT, null, null)

        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].popupType).isEqualTo(Popup.PopupType.EVENT)
    }

    @Test
    fun `목록 - isActive 필터`() {
        adminFixtures.popup(title = "활성", isActive = true)
        adminFixtures.popup(title = "비활성", isActive = false)

        val page = sut.list(PageRequest.of(0, 10), null, true, null)

        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].isActive).isTrue()
    }

    @Test
    fun `목록 - keyword 는 제목 부분일치 검색`() {
        adminFixtures.popup(title = "긴급 팝업")
        adminFixtures.popup(title = "일반 팝업")

        val page = sut.list(PageRequest.of(0, 10), null, null, "긴급")

        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].title).isEqualTo("긴급 팝업")
    }

    @Test
    fun `목록 - 페이징`() {
        adminFixtures.popup(title = "팝업1")
        adminFixtures.popup(title = "팝업2")
        adminFixtures.popup(title = "팝업3")

        val page = sut.list(PageRequest.of(0, 2), null, null, null)

        assertThat(page.content).hasSize(2)
        assertThat(page.page.totalElements).isEqualTo(3)
        assertThat(page.page.totalPages).isEqualTo(2)
    }

    @Test
    fun `등록 - 팝업을 생성한다`() {
        val result = sut.create(
            PopupSaveRequest(
                popupType = Popup.PopupType.NOTICE,
                title = "새 팝업",
                content = "내용",
                imageUrl = null,
                startDateTime = LocalDateTime.of(2026, 6, 1, 0, 0),
                endDateTime = LocalDateTime.of(2026, 6, 10, 0, 0),
                isActive = true,
                targetVersion = null,
            ),
        )

        assertThat(result.popupSeq).isPositive()
        assertThat(result.title).isEqualTo("새 팝업")
        assertThat(result.popupType).isEqualTo(Popup.PopupType.NOTICE)
    }

    @Test
    fun `수정 - 팝업 정보를 변경한다`() {
        val popup = adminFixtures.popup(title = "원래 제목")

        val result = sut.update(
            popup.popupSeq,
            PopupSaveRequest(
                popupType = Popup.PopupType.EVENT,
                title = "변경된 제목",
                content = "변경된 내용",
                imageUrl = "https://example.com/image.png",
                startDateTime = LocalDateTime.of(2026, 7, 1, 0, 0),
                endDateTime = LocalDateTime.of(2026, 7, 10, 0, 0),
                isActive = false,
                targetVersion = "2.0.0",
            ),
        )

        assertThat(result.title).isEqualTo("변경된 제목")
        assertThat(result.popupType).isEqualTo(Popup.PopupType.EVENT)
        assertThat(result.isActive).isFalse()
        assertThat(result.targetVersion).isEqualTo("2.0.0")
    }

    @Test
    fun `수정 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy {
            sut.update(
                999_999L,
                PopupSaveRequest(
                    popupType = Popup.PopupType.NOTICE,
                    title = "제목",
                    content = "내용",
                    startDateTime = LocalDateTime.now(),
                    endDateTime = LocalDateTime.now().plusDays(1),
                    isActive = true,
                ),
            )
        }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제 - 팝업을 삭제한다`() {
        val popup = adminFixtures.popup()

        sut.delete(popup.popupSeq)

        assertThat(popupAdminRepository.existsById(popup.popupSeq)).isFalse()
    }

    @Test
    fun `삭제 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.delete(999_999L) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `토글 - 활성에서 비활성으로 변경한다`() {
        val popup = adminFixtures.popup(isActive = true)

        val result = sut.toggleActive(popup.popupSeq, false)

        assertThat(result.isActive).isFalse()
    }

    @Test
    fun `토글 - 비활성에서 활성으로 변경한다`() {
        val popup = adminFixtures.popup(isActive = false)

        val result = sut.toggleActive(popup.popupSeq, true)

        assertThat(result.isActive).isTrue()
    }

    @Test
    fun `토글 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.toggleActive(999_999L, true) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
}
