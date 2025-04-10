package com.fillsa.fillsa_api.domain.notice.service.useCase

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.notice.dto.NoticeResponse
import org.springframework.data.domain.Pageable

interface NoticeUseCase {
    /**
     *  공지사항 목록 조회
     */
    fun getNotices(pageable: Pageable): PageResponse<NoticeResponse>
}