package com.fillsa.admin.service.notice

import com.fillsa.admin.fixture.AdminFixtures
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoticeAdminServiceTest @Autowired constructor(
    private val sut: NoticeAdminService,
    private val adminFixtures: AdminFixtures,
    private val noticeAdminRepository: NoticeAdminQueryRepository,
) {

    @Test
    fun `목록 - 기본 정렬 noticeSeq desc`() {
        val n1 = adminFixtures.notice(title = "첫번째 공지")
        val n2 = adminFixtures.notice(title = "두번째 공지")

        val page = sut.list(PageRequest.of(0, 10), null)

        assertThat(page.content).hasSize(2)
        // 기본 정렬 noticeSeq desc → 나중에 저장한 n2 가 먼저
        assertThat(page.content[0].noticeSeq).isEqualTo(n2.noticeSeq)
        assertThat(page.content[1].noticeSeq).isEqualTo(n1.noticeSeq)
        assertThat(page.page.totalElements).isEqualTo(2)
    }

    @Test
    fun `목록 - keyword 는 제목 부분일치 검색`() {
        adminFixtures.notice(title = "긴급 공지")
        adminFixtures.notice(title = "일반 안내")

        val result = sut.list(PageRequest.of(0, 10), "긴급")

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).isEqualTo("긴급 공지")
    }

    @Test
    fun `목록 - 페이징`() {
        adminFixtures.notice(title = "공지1")
        adminFixtures.notice(title = "공지2")
        adminFixtures.notice(title = "공지3")

        val page = sut.list(PageRequest.of(0, 2), null)

        assertThat(page.content).hasSize(2)
        assertThat(page.page.totalElements).isEqualTo(3)
        assertThat(page.page.totalPages).isEqualTo(2)
    }

    @Test
    fun `등록 - 공지를 생성한다`() {
        val result = sut.create(NoticeSaveRequest(title = "새 공지", content = "내용"))

        assertThat(result.noticeSeq).isPositive()
        assertThat(result.title).isEqualTo("새 공지")
        assertThat(result.content).isEqualTo("내용")
    }

    @Test
    fun `수정 - 제목과 내용을 변경한다`() {
        val notice = adminFixtures.notice(title = "원래 제목", content = "원래 내용")

        val result = sut.update(notice.noticeSeq, NoticeSaveRequest(title = "변경된 제목", content = "변경된 내용"))

        assertThat(result.title).isEqualTo("변경된 제목")
        assertThat(result.content).isEqualTo("변경된 내용")
    }

    @Test
    fun `수정 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy {
            sut.update(999_999L, NoticeSaveRequest(title = "제목", content = "내용"))
        }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `삭제 - 공지를 삭제한다`() {
        val notice = adminFixtures.notice()

        sut.delete(notice.noticeSeq)

        assertThat(noticeAdminRepository.existsById(notice.noticeSeq)).isFalse()
    }

    @Test
    fun `삭제 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.delete(999_999L) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
}
