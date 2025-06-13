package store.fillsa.fillsa_api.domain.notice.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.dto.PageResponse
import store.fillsa.fillsa_api.domain.notice.dto.NoticeResponse
import store.fillsa.fillsa_api.domain.notice.repository.NoticeRepository

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository
) {

    @Transactional(readOnly = true)
    fun getNotices(pageable: Pageable): PageResponse<NoticeResponse> {
        val pagingNotices = noticeRepository.findByPageable(pageable)

        return PageResponse.fromPage(pagingNotices) { notice ->
            NoticeResponse.from(notice)
        }
    }
}