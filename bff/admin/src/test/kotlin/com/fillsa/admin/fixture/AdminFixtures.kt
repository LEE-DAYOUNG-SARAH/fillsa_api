package com.fillsa.admin.fixture

import com.fillsa.admin.service.dailyquote.DailyQuoteAdminQueryRepository
import com.fillsa.admin.service.notice.NoticeAdminQueryRepository
import com.fillsa.admin.service.popup.PopupAdminQueryRepository
import com.fillsa.admin.service.quote.QuoteAdminQueryRepository
import com.fillsa.service.admin.AdminEntity
import com.fillsa.service.admin.AdminRepository
import com.fillsa.service.admin.AdminRole
import com.fillsa.service.appversion.AppVersion
import com.fillsa.service.appversion.AppVersionRepository
import com.fillsa.service.notice.Notice
import com.fillsa.service.popup.Popup
import com.fillsa.service.quote.DailyQuote
import com.fillsa.service.quote.Quote
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class AdminFixtures(
    private val adminRepository: AdminRepository,
    private val quoteAdminRepository: QuoteAdminQueryRepository,
    private val dailyQuoteAdminRepository: DailyQuoteAdminQueryRepository,
    private val noticeAdminRepository: NoticeAdminQueryRepository,
    private val popupAdminRepository: PopupAdminQueryRepository,
    private val appVersionRepository: AppVersionRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun admin(
        loginId: String = "admin-" + System.nanoTime(),
        rawPassword: String = "password1234",
        name: String = "관리자",
        role: AdminRole = AdminRole.ADMIN,
        activeYn: String = "Y",
    ): AdminEntity = adminRepository.save(
        AdminEntity(
            loginId = loginId,
            password = passwordEncoder.encode(rawPassword),
            name = name,
            role = role,
            activeYn = activeYn,
        ),
    )

    fun quote(
        korQuote: String? = "한국어 명언",
        engQuote: String? = "English quote",
        korAuthor: String? = "한국 작가",
        engAuthor: String? = "English Author",
        category: String? = "카테고리",
        delYn: String = "N",
    ): Quote = quoteAdminRepository.save(
        Quote(
            korQuote = korQuote,
            engQuote = engQuote,
            korAuthor = korAuthor,
            engAuthor = engAuthor,
            category = category,
            delYn = delYn,
        ),
    )

    fun dailyQuote(
        quote: Quote,
        quoteDate: LocalDate = LocalDate.now(),
        quoteDayOfWeek: String = "월",
    ): DailyQuote = dailyQuoteAdminRepository.save(
        DailyQuote(quote = quote, quoteDate = quoteDate, quoteDayOfWeek = quoteDayOfWeek),
    )

    fun notice(
        title: String = "공지 제목",
        content: String = "공지 내용",
    ): Notice = noticeAdminRepository.save(
        Notice(title = title, content = content),
    )

    fun popup(
        popupType: Popup.PopupType = Popup.PopupType.NOTICE,
        title: String = "팝업 제목",
        content: String = "팝업 내용",
        imageUrl: String? = null,
        startDateTime: LocalDateTime = LocalDateTime.now(),
        endDateTime: LocalDateTime = LocalDateTime.now().plusDays(7),
        isActive: Boolean = true,
        targetVersion: String? = null,
    ): Popup = popupAdminRepository.save(
        Popup(
            popupType = popupType,
            title = title,
            content = content,
            imageUrl = imageUrl,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            isActive = isActive,
            targetVersion = targetVersion,
        ),
    )

    fun appVersion(
        minVersion: String = "1.0.0",
        nowVersion: String = "1.0.0",
    ): AppVersion = appVersionRepository.save(
        AppVersion(minVersion = minVersion, nowVersion = nowVersion),
    )
}
