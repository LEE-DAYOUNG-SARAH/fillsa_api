package com.fillsa.admin.service.memberquote

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.admin.service.dailyquote.DailyQuoteAdminQueryRepository
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MemberQuoteAdminService(
    private val memberQuoteAdminRepository: MemberQuoteAdminQueryRepository,
    private val dailyQuoteAdminRepository: DailyQuoteAdminQueryRepository,
) {

    @Transactional(readOnly = true)
    fun list(
        pageable: Pageable,
        keyword: String?,
        quoteDate: LocalDate?,
        completed: Boolean?,
    ): PageEnvelope<MemberQuoteListItem> {
        val page = memberQuoteAdminRepository.search(
            keyword = keyword?.takeIf { it.isNotBlank() },
            quoteDate = quoteDate,
            completed = completed,
            pageable = withDefaultSort(pageable),
        )

        return PageEnvelope.from(page, MemberQuoteListItem::from)
    }

    @Transactional(readOnly = true)
    fun detail(memberQuoteSeq: Long): MemberQuoteDetailResponse {
        val memberQuote = memberQuoteAdminRepository.findDetailByMemberQuoteSeq(memberQuoteSeq)
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 memberQuoteSeq: $memberQuoteSeq")

        val assignedCount = dailyQuoteAdminRepository.countByQuoteQuoteSeq(memberQuote.dailyQuote.quote.quoteSeq).toInt()
        return MemberQuoteDetailResponse.from(memberQuote, assignedCount)
    }

    /**
     * quoteDate 는 MemberQuote 엔티티의 직접 필드가 아니라 dailyQuote 연관관계를 거친 값이라
     * Pageable 의 sort 프로퍼티명(quoteDate)을 JPA 경로(dailyQuote.quoteDate)로 치환해야 한다.
     */
    private fun withDefaultSort(pageable: Pageable): Pageable =
        if (pageable.sort.isSorted) {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, resolveSort(pageable.sort))
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "dailyQuote.quoteDate"))
        }

    private fun resolveSort(sort: Sort): Sort =
        Sort.by(
            sort.map { order ->
                val property = if (order.property == "quoteDate") "dailyQuote.quoteDate" else order.property
                Sort.Order(order.direction, property)
            }.toList(),
        )
}
