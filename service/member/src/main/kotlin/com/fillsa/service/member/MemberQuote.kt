package com.fillsa.service.member

import jakarta.persistence.*
import com.fillsa.util.entity.BaseEntity
import com.fillsa.service.member.Member
import com.fillsa.service.quote.DailyQuote
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "member_quotes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["MEMBER_SEQ", "DAILY_QUOTE_SEQ"])]
)
class MemberQuote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val memberQuoteSeq: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_SEQ", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DAILY_QUOTE_SEQ", nullable = false)
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
    var likeYn: String = "N",

    @Column(nullable = false)
    var completed: Boolean = false,

    @Column(nullable = false)
    var todayCompleted: Boolean = false,

    /** 오늘의 질문 답변 (최대 200자). 필사 완료·연속에는 반영하지 않는다 — docs/home-renewal-api-plan.md §11-1 */
    @Column(nullable = true, length = 200)
    var answer: String? = null,

    @Column(nullable = true)
    var answeredAt: LocalDateTime? = null
): BaseEntity() {
    fun updateImagePath(imagePath: String?) {
        this.imagePath = imagePath
    }

    fun updateTypingQuote(kor: String?, eng: String?) {
        this.typingKorQuote = kor
        this.typingEngQuote = eng
    }

    fun updateMemo(memo: String?) {
        this.memo = memo
    }

    fun updateLikeYn(likeYn: String) {
        this.likeYn = likeYn
    }

    /**
     * 오늘의 질문 답변 등록·수정.
     * 답변은 필사 완료(completed)·연속(streak)에 영향을 주지 않는다 — 별개 기록이다.
     */
    fun updateAnswer(answer: String) {
        this.answer = answer
        this.answeredAt = LocalDateTime.now()
    }

    fun hasAnswer() = !answer.isNullOrBlank()

    fun getTypingYn() = if(hasTypingQuotes() || hasImgPath()) "Y" else "N"

    private fun hasTypingQuotes() = !typingKorQuote.isNullOrEmpty() || !typingEngQuote.isNullOrEmpty()

    private fun hasImgPath() = !imagePath.isNullOrEmpty()

    fun hasContent() = getTypingYn() == "Y" || !imagePath.isNullOrEmpty() || likeYn == "Y"

    fun complete(quoteDate: LocalDate) {
        if(isToday(quoteDate)) {
            this.todayCompleted = true
        }
        this.completed = true
    }

    fun shouldMarkImageCompleted(imagePath: String?): Boolean {
        return !imagePath.isNullOrBlank() && !todayCompleted
    }

    fun shouldMarkTypingCompleted(
        typingKorQuote: String?,
        typingEngQuote: String?,
        korQuote: String?,
        engQuote: String?
    ): Boolean {
        return hasValidTyping(typingKorQuote, typingEngQuote, korQuote, engQuote) && !todayCompleted
    }

    private fun isToday(quoteDate: LocalDate): Boolean = quoteDate.isEqual(LocalDate.now())

    private fun hasValidTyping(
        typingKorQuote: String?,
        typingEngQuote: String?,
        korQuote: String?,
        engQuote: String?
    ): Boolean {
        return listOf(
            typingKorQuote to korQuote,
            typingEngQuote to engQuote
        ).any { (typed, original) ->
            typed != null && original != null && typed == original
        }
    }

    /**
     * 조회 응답에 실어줄 대상인지. '필사 완료' 판정과는 별개다.
     * 답변만 작성한 날도 응답에 포함되어야 한다 — docs/home-renewal-api-plan.md §5-2
     */
    fun isViewQuoteData() = completed || likeYn == "Y" || hasAnswer()
}