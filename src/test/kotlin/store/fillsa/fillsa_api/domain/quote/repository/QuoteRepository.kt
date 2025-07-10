package store.fillsa.fillsa_api.domain.quote.repository

import org.springframework.data.jpa.repository.JpaRepository
import store.fillsa.fillsa_api.domain.quote.entity.Quote

/**
 * 테스트용 QuoteRepository
 * 실제 서비스 로직에서는 Quote를 직접 DB에 저장하지만,
 * 테스트에서는 이 Repository를 사용하여 Quote 엔티티를 관리합니다.
 */
interface QuoteRepository : JpaRepository<Quote, Long> {
} 