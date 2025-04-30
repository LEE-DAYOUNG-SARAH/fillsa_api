package com.fillsa.fillsa_api.domain.quote.repository

import com.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import org.springframework.data.jpa.repository.JpaRepository

interface DailyQuoteRepository: JpaRepository<DailyQuote, Long> {
}