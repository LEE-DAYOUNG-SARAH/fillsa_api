package store.fillsa.fillsa_api.domain.members.quote.entity

import store.fillsa.fillsa_api.common.entity.BaseEntity
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import jakarta.persistence.*

@Entity
@Table(name = "member_quotes")
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
    var likeYn: String = "N"
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

    fun getTypingYn(): String {
        return if(typingKorQuote != null && typingEngQuote != null) "Y" else "N"
    }

    fun updateLikeYn(likeYn: String) {
        this.likeYn = likeYn
    }
}