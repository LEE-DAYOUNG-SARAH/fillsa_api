package com.fillsa.admin.service.quote

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.admin.service.dailyquote.DailyQuoteAdminQueryRepository
import com.fillsa.service.quote.Quote
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuoteAdminService(
    private val quoteAdminRepository: QuoteAdminQueryRepository,
    private val dailyQuoteAdminRepository: DailyQuoteAdminQueryRepository,
) {

    @Transactional(readOnly = true)
    fun list(pageable: Pageable, keyword: String?, category: String?): PageEnvelope<QuoteResponse> {
        val effectivePageable = withDefaultSort(pageable)
        val page = quoteAdminRepository.search(
            keyword = keyword?.takeIf { it.isNotBlank() },
            category = category?.takeIf { it.isNotBlank() },
            pageable = effectivePageable,
        )

        val assignedCounts = assignedCounts(page.content.map { it.quoteSeq })

        return PageEnvelope.from(page) { quote ->
            QuoteResponse.from(quote, assignedCounts[quote.quoteSeq]?.toInt() ?: 0)
        }
    }

    @Transactional
    fun create(request: QuoteSaveRequest): QuoteResponse {
        val quote = Quote(
            korQuote = request.korQuote,
            engQuote = request.engQuote,
            korAuthor = request.korAuthor,
            engAuthor = request.engAuthor,
            category = null, // 카테고리 기능 제거 — 항상 null 저장
            questionKo = request.questionKo,
            questionEn = request.questionEn,
        )
        val saved = quoteAdminRepository.save(quote)
        return QuoteResponse.from(saved, 0)
    }

    @Transactional
    fun update(quoteSeq: Long, request: QuoteSaveRequest): QuoteResponse {
        val quote = getActiveQuote(quoteSeq)
        quote.korQuote = request.korQuote
        quote.engQuote = request.engQuote
        quote.korAuthor = request.korAuthor
        quote.engAuthor = request.engAuthor
        quote.category = null // 카테고리 기능 제거 — 항상 null 저장
        quote.questionKo = request.questionKo
        quote.questionEn = request.questionEn

        val assignedCount = dailyQuoteAdminRepository.countByQuoteQuoteSeq(quoteSeq).toInt()
        return QuoteResponse.from(quote, assignedCount)
    }

    @Transactional
    fun delete(quoteSeq: Long) {
        val quote = getActiveQuote(quoteSeq)

        val assignedCount = dailyQuoteAdminRepository.countByQuoteQuoteSeq(quoteSeq)
        if (assignedCount > 0) {
            throw BusinessException(ErrorCode.CONFLICT, "배정 이력이 있어 삭제할 수 없습니다. (assignedCount=$assignedCount)")
        }

        quote.softDelete()
    }

    private fun getActiveQuote(quoteSeq: Long): Quote =
        quoteAdminRepository.findByQuoteSeqAndDelYn(quoteSeq, "N")
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 quoteSeq: $quoteSeq")

    private fun assignedCounts(quoteSeqs: List<Long>): Map<Long, Long> {
        if (quoteSeqs.isEmpty()) return emptyMap()
        return dailyQuoteAdminRepository.countAssignedByQuoteSeqs(quoteSeqs)
            .associate { it.quoteSeq to it.assignedCount }
    }

    private fun withDefaultSort(pageable: Pageable): Pageable =
        if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "quoteSeq"))
        }
}
