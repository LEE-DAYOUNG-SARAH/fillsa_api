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

    /** AI 회고 질문 (한국어) — 명언별 저널링 질문. [[회고질문]] 기획 */
    @Column(name = "QUESTION_KO", nullable = true, columnDefinition = "text")
    var questionKo: String? = null,

    /** AI 회고 질문 (영어) */
    @Column(name = "QUESTION_EN", nullable = true, columnDefinition = "text")
    var questionEn: String? = null,

    /** 소프트 삭제 플래그. 'Y' 이면 앱 조회에서 제외된다. */
    @Column(name = "DEL_YN", nullable = false, columnDefinition = "char(1)")
    var delYn: String = "N",
): BaseEntity() {
    fun isDeleted(): Boolean = delYn == "Y"

    fun softDelete() {
        this.delYn = "Y"
    }
}