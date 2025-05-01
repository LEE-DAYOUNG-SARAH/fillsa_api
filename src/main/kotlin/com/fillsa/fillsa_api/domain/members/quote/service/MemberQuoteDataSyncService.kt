package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import com.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteDataSyncUseCase
import com.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberQuoteDataSyncService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteRepository: DailyQuoteRepository
): MemberQuoteDataSyncUseCase {
    val log = KotlinLogging.logger {  }

    @Transactional
    override fun syncData(member: Member, request: List<LoginRequest.SyncData>) {
        try {
            val dailyQuotes = dailyQuoteRepository.findAllById(request.map { it.dailyQuoteSeq })
            val memberQuotes = memberQuoteRepository.findAllByMemberAndDailyQuoteIn(member, dailyQuotes)

            val saveMemberQuotes = request.mapNotNull { data ->
                val dailyQuote = dailyQuotes.find { it.dailyQuoteSeq == data.dailyQuoteSeq }
                    ?: return@mapNotNull null

                val memberQuote = memberQuotes.find { it.dailyQuote == dailyQuote }
                    ?: MemberQuote(
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