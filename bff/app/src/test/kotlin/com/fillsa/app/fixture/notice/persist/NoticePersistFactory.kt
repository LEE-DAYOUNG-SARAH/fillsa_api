package com.fillsa.app.fixture.notice.persist

import org.springframework.stereotype.Component
import com.fillsa.service.notice.Notice
import com.fillsa.service.notice.NoticeRepository
import com.fillsa.app.fixture.notice.entity.NoticeEntityFactory

@Component
class NoticePersistFactory(
    private val noticeRepository: NoticeRepository
) {
    fun createNotice(notice: Notice = NoticeEntityFactory.notice()): Notice {
        return noticeRepository.save(notice)
    }
} 