package store.fillsa.fillsa_api.domain.notice.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.notice.entity.Notice
import java.time.LocalDateTime

data class NoticeResponse(
    @Schema(description = "공지사항 일련번호", required = true)
    val noticeSeq: Long,

    @Schema(description = "제목", required = true)
    val title: String,

    @Schema(description = "내용", required = true)
    val content: String,

    @Schema(description = "생성일시", example = "yyyy.MM.dd", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notice: Notice) = NoticeResponse(
            noticeSeq = notice.noticeSeq,
            title = notice.title,
            content = notice.content,
            createdAt = notice.createdAt
        )
    }
}