package com.fillsa.app.service.notice

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.common.dto.PageResponse
import com.fillsa.app.api.notice.NoticeResponse
import com.fillsa.service.notice.NoticeRepository

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