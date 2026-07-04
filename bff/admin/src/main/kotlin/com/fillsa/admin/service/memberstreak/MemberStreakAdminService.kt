package com.fillsa.admin.service.memberstreak

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.admin.common.dto.PageEnvelope
import java.time.LocalDate

@Service
class MemberStreakAdminService(
    private val memberStreakAdminRepository: MemberStreakAdminQueryRepository,
) {

    @Transactional(readOnly = true)
    fun list(pageable: Pageable, keyword: String?): PageEnvelope<MemberStreakListItem> {
        val today = LocalDate.now()
        val page = memberStreakAdminRepository.search(
            keyword = keyword?.takeIf { it.isNotBlank() },
            pageable = withDefaultSort(pageable),
        )

        return PageEnvelope.from(page) { memberStreak -> MemberStreakListItem.from(memberStreak, today) }
    }

    @Transactional(readOnly = true)
    fun summary(): StreakSummaryResponse {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        return StreakSummaryResponse.from(
            maxStreak = memberStreakAdminRepository.findMaxStreak(),
            avgCurrentStreak = memberStreakAdminRepository.findAvgCurrentStreak(today, yesterday),
            todayWrittenCount = memberStreakAdminRepository.countByLastWrittenDate(today),
            over30Count = memberStreakAdminRepository.countOver30(today, yesterday),
        )
    }

    private fun withDefaultSort(pageable: Pageable): Pageable =
        if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "currentStreak"))
        }
}
