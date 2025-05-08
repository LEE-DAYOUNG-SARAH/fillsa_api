package store.fillsa.fillsa_api.domain.quote.entity

import store.fillsa.fillsa_api.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "quotes")
class Quote (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val quoteSeq: Long = 0L,

    @Column(nullable = true)
    var korQuote: String? = null,

    @Column(nullable = true)
    var engQuote: String? = null,

    @Column(nullable = true)
    var korAuthor: String? = null,

    @Column(nullable = true)
    var engAuthor: String? = null,

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    var category: QuoteCategory? = null,
): BaseEntity() {
    enum class QuoteCategory(val value: String) {
        DEATH("death"),
        FAITH("faith"),
        HOPE("hope"),
        HUMOR("humor"),
        INSPIRATIONAL("inspirational"),
        LIVE("live"),
        LIVE_QUOTES("live-quotes"),
        LOVE("love"),
        MOTIVATIONAL("motivational"),
        POETRY("poetry"),
        SCIENCE("science"),
        SUCCESS("success"),
        TIME("time"),
        WISDOM("wisdom"),
        WRITING("writing");

        override fun toString(): String = value

        companion object {
            fun fromValue(value: String?): QuoteCategory? {
                if (value == null) return null
                return entries.find { it.value == value }
            }
        }
    }
}