package com.fillsa.admin.service.notice

import com.fillsa.service.notice.Notice
import java.time.LocalDateTime

data class NoticeSaveRequest(
    val title: String,
    val content: String,
)

data class NoticeResponse(
    val noticeSeq: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(notice: Notice) = NoticeResponse(
            noticeSeq = notice.noticeSeq,
            title = notice.title,
            content = notice.content,
            createdAt = notice.createdAt,
            updatedAt = notice.updatedAt,
        )
    }
}
