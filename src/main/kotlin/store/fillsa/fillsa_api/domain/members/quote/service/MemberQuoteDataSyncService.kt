package store.fillsa.fillsa_api.domain.members.quote.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import store.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository

@Service
class MemberQuoteDataSyncService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteRepository: DailyQuoteRepository
) {
    val log = KotlinLogging.logger {  }

    @Transactional
    fun syncData(member: Member, request: List<LoginRequest.SyncData>) {
        try {
            val dailyQuotes = dailyQuoteRepository.findAllById(request.map { it.dailyQuoteSeq })
            val memberQuotes = memberQuoteRepository.findAllByMemberAndDailyQuoteIn(member, dailyQuotes)

            val saveMemberQuotes = request.mapNotNull { data ->
                val dailyQuote = dailyQuotes.find { it.dailyQuoteSeq == data.dailyQuoteSeq }
                    ?: return@mapNotNull null

                val memberQuote = memberQuotes.find { it.dailyQuote == dailyQuote }
                    ?: store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote(
                        dailyQuote = dailyQuote,
                        member = member
                    )

                memberQuote.updateTypingQuote(data.typingQuoteRequest.typingKorQuote, data.typingQuoteRequest.typingEngQuote)
                memberQuote.updateMemo(data.memoRequest.memo)
                memberQuote.updateLikeYn(data.likeRequest.likeYn)

                memberQuote
            }

            memberQuoteRepository.saveAll(saveMemberQuotes)
        } catch (e: Exception) {
            log.error("Failed to sync data for member: ${member.memberSeq}", e)
            throw e
        }
    }
}