package com.fillsa.fillsa_api.domain.members.quote.entity

import com.fillsa.fillsa_api.common.entity.BaseEntity
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import jakarta.persistence.*

@Entity
@Table(name = "member_quotes")
class MemberQuote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val memberQuoteSeq: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_SEQ")
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DAILY_QUOTE_SEQ")
    val dailyQuote: DailyQuote,

    @Column(nullable = true)
    var typingKorQuote: String? = null,

    @Column(nullable = true)
    var typingEngQuote: String? = null,

    @Column(nullable = true)
    var imagePath: String? = null,

    @Column(nullable = true)
    var memo: String? = null,

    @Column(nullable = false, columnDefinition = "char(1)")
    var likeYn: String,
): BaseEntity()