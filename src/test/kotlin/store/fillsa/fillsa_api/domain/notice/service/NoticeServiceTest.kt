package store.fillsa.fillsa_api.domain.notice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.PageRequest
import store.fillsa.fillsa_api.fixture.notice.entity.NoticeEntityFactory
import store.fillsa.fillsa_api.fixture.notice.persist.NoticePersistFactory
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoticeServiceTest @Autowired constructor(
    private val noticePersistFactory: NoticePersistFactory,
    private val sut: NoticeService
) {
    
    @Test
    fun `공지사항 목록 조회 성공 - 페이지네이션된 공지사항 목록을 반환한다`() {
        // given
        val pageable = PageRequest.of(0, 10)
        val notice1 = noticePersistFactory.createNotice(
            NoticeEntityFactory.notice(
                title = "공지사항 1",
                content = "내용 1"
            )
        )
        val notice2 = noticePersistFactory.createNotice(
            NoticeEntityFactory.notice(
                title = "공지사항 2",
                content = "내용 2"
            )
        )
        
        // when
        val result = sut.getNotices(pageable)
        
        // then
        assertThat(result.content).hasSize(2)
        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.currentPage).isEqualTo(0)
        
        val titles = result.content.map { it.title }
        val contents = result.content.map { it.content }
        
        assertThat(titles).contains(notice1.title, notice2.title)
        assertThat(contents).contains(notice1.content, notice2.content)
    }
    
    @Test
    fun `공지사항 목록 조회 성공 - 공지사항이 없는 경우 빈 목록을 반환한다`() {
        // given
        val pageable = PageRequest.of(0, 10)
        
        // when
        val result = sut.getNotices(pageable)
        
        // then
        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(0)
        assertThat(result.totalPages).isEqualTo(0)
        assertThat(result.currentPage).isEqualTo(0)
    }
    
    @Test
    fun `공지사항 목록 조회 성공 - 두 번째 페이지를 조회한다`() {
        // given
        val pageable = PageRequest.of(1, 2)
        
        repeat(5) { index ->
            noticePersistFactory.createNotice(
                NoticeEntityFactory.notice(
                    title = "공지사항 ${index + 1}",
                    content = "내용 ${index + 1}"
                )
            )
        }
        
        // when
        val result = sut.getNotices(pageable)
        
        // then
        assertThat(result.content).hasSize(2) // Second page with size 2
        assertThat(result.totalElements).isEqualTo(5)
        assertThat(result.totalPages).isEqualTo(3)
        assertThat(result.currentPage).isEqualTo(1)
    }
    
    @Test
    fun `공지사항 목록 조회 성공 - 제목과 내용이 올바르게 반환된다`() {
        // given
        val pageable = PageRequest.of(0, 10)
        val expectedTitle = "중요한 공지사항"
        val expectedContent = "이것은 매우 중요한 내용입니다."
        
        noticePersistFactory.createNotice(
            NoticeEntityFactory.notice(
                title = expectedTitle,
                content = expectedContent
            )
        )
        
        // when
        val result = sut.getNotices(pageable)
        
        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).isEqualTo(expectedTitle)
        assertThat(result.content[0].content).isEqualTo(expectedContent)
        assertThat(result.content[0].createdAt).isNotNull()
    }
    
    @Test
    fun `공지사항 목록 조회 성공 - 대량 데이터 페이지네이션 테스트`() {
        // given
        val pageable = PageRequest.of(2, 3)
        
        repeat(10) { index ->
            noticePersistFactory.createNotice(
                NoticeEntityFactory.notice(
                    title = "공지사항 ${index + 1}",
                    content = "내용 ${index + 1}"
                )
            )
        }
        
        // when
        val result = sut.getNotices(pageable)
        
        // then
        assertThat(result.content).hasSize(3)
        assertThat(result.totalElements).isEqualTo(10)
        assertThat(result.totalPages).isEqualTo(4)
        assertThat(result.currentPage).isEqualTo(2)
    }
} 