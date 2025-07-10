package store.fillsa.fillsa_api.fixture.notice.persist

import org.springframework.stereotype.Component
import store.fillsa.fillsa_api.domain.notice.entity.Notice
import store.fillsa.fillsa_api.domain.notice.repository.NoticeRepository
import store.fillsa.fillsa_api.fixture.notice.entity.NoticeEntityFactory

@Component
class NoticePersistFactory(
    private val noticeRepository: NoticeRepository
) {
    fun createNotice(notice: Notice = NoticeEntityFactory.notice()): Notice {
        return noticeRepository.save(notice)
    }
} 