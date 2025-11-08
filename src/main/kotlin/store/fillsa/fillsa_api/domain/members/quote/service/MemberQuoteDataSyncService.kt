package store.fillsa.fillsa_api.domain.members.quote.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import store.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository

@Service
class MemberQuoteDataSyncService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val memberStreakService: MemberStreakService
) {
    val log = KotlinLogging.logger {  }

    @Transactional
    fun syncData(member: Member, request: List<LoginRequest.SyncData>) {
        try {
            val dailyQuotes = dailyQuoteRepository.findAllByDailQuoteSeqIn(request.map { it.dailyQuoteSeq })
            val memberQuotes = memberQuoteRepository.findAllByMemberAndDailyQuoteIn(member, dailyQuotes)

            val saveMemberQuotes = request.mapNotNull { data ->
                val dailyQuote = dailyQuotes.find { it.dailyQuoteSeq == data.dailyQuoteSeq }
                    ?: return@mapNotNull null

                val memberQuote = memberQuotes.find { it.dailyQuote == dailyQuote }
                    ?: MemberQuote(
                        dailyQuote = dailyQuote,
                        member = member
                    )

                data.typingQuoteRequest?.let {
                    if(memberQuote.shouldMarkTypingCompleted(
                            it.typingKorQuote,
                            it.typingEngQuote,
                            dailyQuote.quote.korQuote,
                            dailyQuote.quote.engQuote
                    )) {
                        memberQuote.complete(dailyQuote.quoteDate)
                        memberStreakService.recordTodayCompletion(dailyQuote.quoteDate, member)
                    }

                    memberQuote.updateTypingQuote(it.typingKorQuote, it.typingEngQuote)
                }
                data.memoRequest?.let {
                    memberQuote.updateMemo(it.memo)
                }
                data.likeRequest?.let {
                    memberQuote.updateLikeYn(it.likeYn)
                }

                memberQuote
            }

            memberQuoteRepository.saveAll(saveMemberQuotes)
        } catch (e: Exception) {
            log.error("Failed to sync data for member: ${member.memberSeq}", e)
            throw e
        }
    }
}