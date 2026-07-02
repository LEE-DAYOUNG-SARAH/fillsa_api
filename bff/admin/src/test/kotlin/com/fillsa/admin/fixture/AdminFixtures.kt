package com.fillsa.admin.fixture

import com.fillsa.admin.repository.DailyQuoteAdminRepository
import com.fillsa.admin.repository.QuoteAdminRepository
import com.fillsa.service.admin.AdminEntity
import com.fillsa.service.admin.AdminRepository
import com.fillsa.service.admin.AdminRole
import com.fillsa.service.quote.DailyQuote
import com.fillsa.service.quote.Quote
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AdminFixtures(
    private val adminRepository: AdminRepository,
    private val quoteAdminRepository: QuoteAdminRepository,
    private val dailyQuoteAdminRepository: DailyQuoteAdminRepository,
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
}
