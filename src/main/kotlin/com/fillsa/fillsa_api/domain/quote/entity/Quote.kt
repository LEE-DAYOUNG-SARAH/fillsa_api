package com.fillsa.fillsa_api.domain.quote.entity

import com.fillsa.fillsa_api.common.converter.QuoteCategoryConverter
import com.fillsa.fillsa_api.common.entity.BaseEntity
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
    @Convert(converter = QuoteCategoryConverter::class)
    var category: QuoteCategory? = null,
): BaseEntity() {
    enum class QuoteCategory(val value: String) {
        HUMOR("humor"),
        INSPIRATIONAL("inspirational"),
        LIVE_QUOTES("live-quotes"),
        LIVE("live"),
        LOVE("love"),
        MOTIVATIONAL("motivational"),
        SUCCESS("success"),
        WISDOM("wisdom"),
        SCIENCE("science"),
        POETRY("poetry"),
        WRITING("writing"),
        FAITH("faith"),
        TIME("time");

        override fun toString(): String = value

        companion object {
            fun fromValue(value: String?): QuoteCategory? {
                if (value == null) return null
                return entries.find { it.value == value }
            }
        }
    }
}