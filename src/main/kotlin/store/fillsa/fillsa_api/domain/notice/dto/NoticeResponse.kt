package store.fillsa.fillsa_api.domain.notice.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class NoticeResponse(
    @Schema(description = "공지사항 일련번호")
    val noticeSeq: Long,

    @Schema(description = "내용")
    val content: String,

    @Schema(description = "생성일시", example = "yyyy.MM.dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
    val createdAt: LocalDateTime
)