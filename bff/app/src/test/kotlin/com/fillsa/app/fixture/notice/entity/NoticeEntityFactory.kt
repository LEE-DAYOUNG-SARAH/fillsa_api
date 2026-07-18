package com.fillsa.app.fixture.notice.entity

import com.fillsa.service.notice.Notice

class NoticeEntityFactory {
    companion object {
        fun notice(
            title: String = "공지사항 제목",
            content: String = "공지사항 내용입니다."
        ) = Notice(
            title = title,
            content = content
        )
    }
} 