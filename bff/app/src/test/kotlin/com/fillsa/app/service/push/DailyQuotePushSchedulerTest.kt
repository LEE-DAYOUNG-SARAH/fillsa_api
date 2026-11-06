package com.fillsa.app.service.push

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DailyQuotePushSchedulerTest {

    @Test
    fun `본문은 안드로이드 로컬 알림과 동일하게 '문장 - 작가' 포맷이다`() {
        assertThat(DailyQuotePushScheduler.buildBody("삶이 있는 한 희망은 있다", "키케로"))
            .isEqualTo("삶이 있는 한 희망은 있다 - 키케로")
    }

    @Test
    fun `작가가 없으면 문장만 보낸다`() {
        assertThat(DailyQuotePushScheduler.buildBody("삶이 있는 한 희망은 있다", null))
            .isEqualTo("삶이 있는 한 희망은 있다")
        assertThat(DailyQuotePushScheduler.buildBody("삶이 있는 한 희망은 있다", "  "))
            .isEqualTo("삶이 있는 한 희망은 있다")
    }

    @Test
    fun `문장이 없으면 기본 문구를 보낸다`() {
        assertThat(DailyQuotePushScheduler.buildBody(null, "키케로"))
            .isEqualTo("오늘의 명언이 도착했어요")
        assertThat(DailyQuotePushScheduler.buildBody("", "키케로"))
            .isEqualTo("오늘의 명언이 도착했어요")
    }
}
