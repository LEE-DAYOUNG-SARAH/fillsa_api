package com.fillsa.admin.service.notice

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.service.notice.Notice
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeAdminService(
    private val noticeAdminRepository: NoticeAdminQueryRepository,
) {

    @Transactional(readOnly = true)
    fun list(pageable: Pageable, keyword: String?): PageEnvelope<NoticeResponse> {
        val effectivePageable = withDefaultSort(pageable)
        val page = noticeAdminRepository.search(
            keyword = keyword?.takeIf { it.isNotBlank() },
            pageable = effectivePageable,
        )

        return PageEnvelope.from(page) { notice -> NoticeResponse.from(notice) }
    }

    @Transactional
    fun create(request: NoticeSaveRequest): NoticeResponse {
        val notice = Notice(
            title = request.title,
            content = request.content,
        )
        val saved = noticeAdminRepository.save(notice)
        return NoticeResponse.from(saved)
    }

    @Transactional
    fun update(noticeSeq: Long, request: NoticeSaveRequest): NoticeResponse {
        val notice = getNotice(noticeSeq)
        notice.title = request.title
        notice.content = request.content

        return NoticeResponse.from(notice)
    }

    @Transactional
    fun delete(noticeSeq: Long) {
        val notice = getNotice(noticeSeq)
        noticeAdminRepository.delete(notice)
    }

    private fun getNotice(noticeSeq: Long): Notice =
        noticeAdminRepository.findById(noticeSeq)
            .orElseThrow { BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 noticeSeq: $noticeSeq") }

    private fun withDefaultSort(pageable: Pageable): Pageable =
        if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "noticeSeq"))
        }
}
