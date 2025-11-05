package store.fillsa.fillsa_api.migration

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository

@Disabled
@ActiveProfiles("test")
@SpringBootTest
class MemberQuoteCompletedMigration @Autowired constructor(
    val memberQuoteRepository: MemberQuoteRepository
) {

    @Test
    fun `MemberQuote completed 값 초기화 - getTypingYn "Y"인 경우 completed = true`() {
        val allMemberQuotes = memberQuoteRepository.findAll()

        println("총 ${allMemberQuotes.size}개의 MemberQuote 조회 완료")

        var updatedCount = 0
        var skippedCount = 0

        allMemberQuotes.forEach { memberQuote ->
            if (memberQuote.getTypingYn() == "Y") {
                if (!memberQuote.completed) {
                    memberQuote.completed = true
                    updatedCount++
                    println("MemberQuote ${memberQuote.memberQuoteSeq} - completed = true 로 변경")
                } else {
                    skippedCount++
                }
            }
        }

        memberQuoteRepository.saveAll(allMemberQuotes.filter { it.completed })

        println("=== 마이그레이션 완료 ===")
        println("업데이트: ${updatedCount}개")
        println("스킵 (이미 completed=true): ${skippedCount}개")
        println("전체: ${allMemberQuotes.size}개")
    }
}
