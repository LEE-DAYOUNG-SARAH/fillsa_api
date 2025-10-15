package store.fillsa.fillsa_api.domain.members.quote.entity

import jakarta.persistence.*
import store.fillsa.fillsa_api.common.entity.BaseEntity
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import java.time.LocalDate

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
    var todayCompleted: Boolean = false
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

    fun isViewQuoteData() = completed || likeYn == "Y"
}