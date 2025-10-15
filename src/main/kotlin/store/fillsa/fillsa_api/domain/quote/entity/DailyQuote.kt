package store.fillsa.fillsa_api.domain.quote.entity

import store.fillsa.fillsa_api.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "daily_quotes",
    indexes = [Index(name = "idx_daily_quotes_quote_date", columnList = "QUOTE_DATE")]
)
class DailyQuote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val dailyQuoteSeq: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QUOTE_SEQ", nullable = false)
    var quote: Quote,

    @Column(nullable = false)
    var quoteDate: LocalDate,

    @Column(nullable = false, columnDefinition = "char(1)")
    var quoteDayOfWeek: String,
): BaseEntity()