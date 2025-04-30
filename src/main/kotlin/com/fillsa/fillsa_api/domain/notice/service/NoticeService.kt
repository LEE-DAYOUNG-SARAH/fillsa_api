package com.fillsa.fillsa_api.domain.notice.service

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.notice.dto.NoticeResponse
import com.fillsa.fillsa_api.domain.notice.repository.NoticeRepository
import com.fillsa.fillsa_api.domain.notice.service.useCase.NoticeUseCase
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository
): NoticeUseCase {

    @Transactional(readOnly = true)
    override fun getNotices(pageable: Pageable): PageResponse<NoticeResponse> {
        val notices = noticeRepository.findByPageable(pageable)

        val responses = notices.content.map {
            NoticeResponse(
                noticeSeq = it.noticeSeq,
                content = it.content,
                createdAt = it.createdAt
            )
        }

        return PageResponse(
            content = responses,
            totalElements = notices.totalElements,
            totalPages = notices.totalPages,
            currentPage = notices.number
        )
    }
}