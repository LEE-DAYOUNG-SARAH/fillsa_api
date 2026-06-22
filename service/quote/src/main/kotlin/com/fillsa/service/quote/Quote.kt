package com.fillsa.service.quote

import jakarta.persistence.*
import com.fillsa.util.entity.BaseEntity

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
    var category: String? = null,
): BaseEntity()