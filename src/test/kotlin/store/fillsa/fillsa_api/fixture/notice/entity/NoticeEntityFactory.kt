package store.fillsa.fillsa_api.fixture.notice.entity

import store.fillsa.fillsa_api.domain.notice.entity.Notice

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