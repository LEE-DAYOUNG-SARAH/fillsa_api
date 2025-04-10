package com.fillsa.fillsa_api.domain.notice.service

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.notice.dto.NoticeResponse
import com.fillsa.fillsa_api.domain.notice.service.useCase.NoticeUseCase
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NoticeService(): NoticeUseCase {
    override fun getNotices(pageable: Pageable): PageResponse<NoticeResponse> {
        val contents = listOf(
            "새로운 기능 추가 및 버그 수정이 포함된 최신 업데이트가 출시되었습니다. 앱을 최신 버전으로 업데이트해 주세요.",
            "시스템 안정성 강화를 위해 예정된 정기 점검을 실시합니다. 점검 시간 동안 서비스 이용에 불편이 있을 수 있으니 참고 바랍니다.",
            "회원분들을 위한 특별 이벤트가 곧 진행됩니다. 자세한 내용은 앱 공지사항을 통해 추후 안내드리겠습니다."
        )

        return PageResponse<NoticeResponse>(
            content = List(3) { index ->
                NoticeResponse(
                    noticeSeq = index + 1L,
                    content = contents[index],
                    createAt = LocalDateTime.now().minusDays(index + 1L)
                )
            },
            totalElements = 10,
            totalPages = 2,
            currentPage = 0
        )
    }
}