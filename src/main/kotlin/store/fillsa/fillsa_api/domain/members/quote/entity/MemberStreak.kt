package store.fillsa.fillsa_api.domain.members.quote.entity

import jakarta.persistence.*
import store.fillsa.fillsa_api.common.entity.BaseEntity
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import java.time.LocalDate

@Entity
@Table(
    name = "member_streaks",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_member_streaks_member", columnNames = ["MEMBER_SEQ"])
    ]
)
class MemberStreak(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val memberStreakSeq: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_SEQ", nullable = false, unique = true)
    val member: Member,

    @Column(nullable = false)
    var currentStreak: Int = 0,

    @Column(nullable = false)
    var maxStreak: Int = 0,

    @Column(nullable = true)
    var lastWrittenDate: LocalDate? = null
): BaseEntity() {

    fun currentStreakAsOf(now: LocalDate): Int {
        val last = lastWrittenDate ?: return 0
        return when {
            last.isEqual(now) -> currentStreak                // 오늘 했다 → 유지
            last.isEqual(now.minusDays(1)) -> currentStreak   // 어제까지 연속, 오늘은 아직 → 유지
            else -> 0                                         // 그 이전이면 끊김
        }
    }

    fun isTodayWritten(now: LocalDate): Boolean {
        return lastWrittenDate?.isEqual(now) ?: false
    }

    /**
     * 오늘의 문장(=today)에 대해 완료가 발생했을 때만 연속 카운트를 갱신.
     * - 같은 날 중복 호출은 idempotent 처리
     * - 어제에 이어 오늘 완료면 +1, 아니면 1로 리셋
     */
    fun recordTodayCompletion(completedDate: LocalDate) {
        // 이미 오늘 반영되어 있으면 무시
        if (lastWrittenDate == completedDate) return

        val continued = (lastWrittenDate == completedDate.minusDays(1))
        currentStreak = if (continued) currentStreak + 1 else 1
        maxStreak = maxOf(maxStreak, currentStreak)
        lastWrittenDate = completedDate
    }
}