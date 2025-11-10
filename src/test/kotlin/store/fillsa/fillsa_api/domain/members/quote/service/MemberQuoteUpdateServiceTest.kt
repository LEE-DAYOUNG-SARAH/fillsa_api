package store.fillsa.fillsa_api.domain.members.quote.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.domain.members.quote.dto.LikeRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import store.fillsa.fillsa_api.fixture.member.persist.MemberPersistFactory
import store.fillsa.fillsa_api.fixture.quote.entity.QuoteEntityFactory
import store.fillsa.fillsa_api.fixture.quote.persist.QuotePersistFactory

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberQuoteUpdateServiceTest @Autowired constructor(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val memberPersistFactory: MemberPersistFactory,
    private val quotePersistFactory: QuotePersistFactory,
    private val sut: MemberQuoteUpdateService
) {
    
    @Test
    fun `타이핑 명언 업데이트 성공 - 기존 MemberQuote가 있는 경우`() {
        // given
        val (quote, dailyQuote, memberQuote) = quotePersistFactory.createCompleteQuoteSet()
        val request = TypingQuoteRequest(
            typingKorQuote = "한국어 타이핑",
            typingEngQuote = "English Typing"
        )

        // when
        val result = sut.typingQuote(memberQuote.member, dailyQuote.dailyQuoteSeq, request)

        // then
        assertThat(result.memberQuoteSeq).isEqualTo(memberQuote.memberQuoteSeq)
        assertThat(result.completed).isFalse()
        assertThat(result.todayCompleted).isFalse()

        val updatedMemberQuote = memberQuoteRepository.findById(memberQuote.memberQuoteSeq).get()
        assertThat(updatedMemberQuote.typingKorQuote).isEqualTo(request.typingKorQuote)
        assertThat(updatedMemberQuote.typingEngQuote).isEqualTo(request.typingEngQuote)
    }
    
    @Test
    fun `타이핑 명언 업데이트 성공 - 새로운 MemberQuote를 생성하는 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val (quote, dailyQuote) = quotePersistFactory.createQuoteWithDailyQuote()
        val request = TypingQuoteRequest(
            typingKorQuote = "한국어 타이핑",
            typingEngQuote = "English Typing"
        )

        // when
        val result = sut.typingQuote(member, dailyQuote.dailyQuoteSeq, request)

        // then
        assertThat(result).isNotNull()
        assertThat(result.completed).isFalse()
        assertThat(result.todayCompleted).isFalse()

        val createdMemberQuote = memberQuoteRepository.findById(result.memberQuoteSeq).get()
        assertThat(createdMemberQuote.member).isEqualTo(member)
        assertThat(createdMemberQuote.dailyQuote).isEqualTo(dailyQuote)
        assertThat(createdMemberQuote.typingKorQuote).isEqualTo(request.typingKorQuote)
        assertThat(createdMemberQuote.typingEngQuote).isEqualTo(request.typingEngQuote)
    }
    
    @Test
    fun `타이핑 명언 업데이트 실패 - 존재하지 않는 dailyQuoteSeq인 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val nonExistentDailyQuoteSeq = 999L
        val request = TypingQuoteRequest(
            typingKorQuote = "한국어 타이핑",
            typingEngQuote = "English Typing"
        )
        
        // when & then
        assertThatThrownBy { sut.typingQuote(member, nonExistentDailyQuoteSeq, request) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
    
    @Test
    fun `메모 업데이트 성공 - 기존 MemberQuote가 있는 경우`() {
        // given
        val (quote, dailyQuote, memberQuote) = quotePersistFactory.createCompleteQuoteSet(
            memberQuote = QuoteEntityFactory.memberQuote(memo = "기존 메모")
        )
        val request = MemoRequest(memo = "테스트 메모")
        
        // when
        val result = sut.memo(memberQuote.member, memberQuote.memberQuoteSeq, request)
        
        // then
        assertThat(result).isEqualTo(memberQuote.memberQuoteSeq)
        
        val updatedMemberQuote = memberQuoteRepository.findById(memberQuote.memberQuoteSeq).get()
        assertThat(updatedMemberQuote.memo).isEqualTo(request.memo)
    }
    
    @Test
    fun `메모 업데이트 실패 - 존재하지 않는 memberQuoteSeq인 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val nonExistentMemberQuoteSeq = 999L
        val request = MemoRequest(memo = "테스트 메모")
        
        // when & then
        assertThatThrownBy { sut.memo(member, nonExistentMemberQuoteSeq, request) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
    
    @Test
    fun `이미지 경로 업데이트 성공`() {
        // given
        val (quote, dailyQuote, memberQuote) = quotePersistFactory.createCompleteQuoteSet(
            memberQuote = QuoteEntityFactory.memberQuote(
                imagePath = "기존 이미지 경로",
                completed = true,
                todayCompleted = true
            )
        )
        val imagePath = "https://example.com/image.jpg"

        // when
        val result = sut.updateImagePath(memberQuote, imagePath)

        // then
        assertThat(result).isNotNull()
        assertThat(result.completedChanged).isFalse()
        assertThat(result.todayCompletedChanged).isFalse()

        val updatedMemberQuote = memberQuoteRepository.findById(memberQuote.memberQuoteSeq).get()
        assertThat(updatedMemberQuote.imagePath).isEqualTo(imagePath)
    }
    
    @Test
    fun `이미지 경로 업데이트 실패 - 존재하지 않는 memberQuoteSeq인 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val (quote, dailyQuote) = quotePersistFactory.createQuoteWithDailyQuote()

        val unsavedMemberQuote = QuoteEntityFactory.memberQuote(
            member = member,
            dailyQuote = dailyQuote
        )

        val imagePath = "https://example.com/image.jpg"

        // when & then
        val exception = assertThrows<BusinessException> {
            sut.updateImagePath(unsavedMemberQuote, imagePath)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.NOT_FOUND)
    }

    @Test
    fun `타이핑 완료 시 completed와 todayCompleted가 true로 변경되는 경우`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val (quote, dailyQuote) = quotePersistFactory.createQuoteWithDailyQuote()
        val request = TypingQuoteRequest(
            typingKorQuote = quote.korQuote,
            typingEngQuote = quote.engQuote
        )

        // when
        val result = sut.typingQuote(member, dailyQuote.dailyQuoteSeq, request)

        // then
        assertThat(result.completed).isTrue()
        assertThat(result.todayCompleted).isTrue()

        val createdMemberQuote = memberQuoteRepository.findById(result.memberQuoteSeq).get()
        assertThat(createdMemberQuote.completed).isTrue()
        assertThat(createdMemberQuote.todayCompleted).isTrue()
    }

    @Test
    fun `이미지 업로드로 완료 시 completed와 todayCompleted가 true로 변경되는 경우`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val (quote, dailyQuote) = quotePersistFactory.createQuoteWithDailyQuote()
        val memberQuote = sut.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = dailyQuote
            )
        )
        val imagePath = "https://example.com/image.jpg"

        // when
        val result = sut.updateImagePath(memberQuote, imagePath)

        // then
        assertThat(result.completedChanged).isTrue()
        assertThat(result.todayCompletedChanged).isTrue()
        assertThat(result.memberQuote.completed).isTrue()
        assertThat(result.memberQuote.todayCompleted).isTrue()
    }

    @Test
    fun `이미 완료된 상태에서 타이핑 업데이트 시 completed와 todayCompleted가 false로 반환되는 경우`() {
        // given
        val (quote, dailyQuote, memberQuote) = quotePersistFactory.createCompleteQuoteSet(
            memberQuote = QuoteEntityFactory.memberQuote(
                completed = true,
                todayCompleted = true
            )
        )
        val request = TypingQuoteRequest(
            typingKorQuote = "새로운 타이핑",
            typingEngQuote = "New Typing"
        )

        // when
        val result = sut.typingQuote(memberQuote.member, dailyQuote.dailyQuoteSeq, request)

        // then
        assertThat(result.completed).isFalse()
        assertThat(result.todayCompleted).isFalse()
    }
    
    @Test
    fun `좋아요 업데이트 성공 - 기존 MemberQuote가 있는 경우`() {
        // given
        val (quote, dailyQuote, memberQuote) = quotePersistFactory.createCompleteQuoteSet(
            memberQuote = QuoteEntityFactory.memberQuote(likeYn = "N")
        )
        val request = LikeRequest(likeYn = "Y")
        
        // when
        val result = sut.like(memberQuote.member, dailyQuote.dailyQuoteSeq, request)
        
        // then
        assertThat(result).isEqualTo(memberQuote.memberQuoteSeq)
        
        val updatedMemberQuote = memberQuoteRepository.findById(memberQuote.memberQuoteSeq).get()
        assertThat(updatedMemberQuote.likeYn).isEqualTo(request.likeYn)
    }
    
    @Test
    fun `좋아요 업데이트 성공 - 새로운 MemberQuote를 생성하는 경우`() {
        // given
        val member = memberPersistFactory.createMember()
        val (quote, dailyQuote) = quotePersistFactory.createQuoteWithDailyQuote()
        val request = LikeRequest(likeYn = "Y")
        
        // when
        val result = sut.like(member, dailyQuote.dailyQuoteSeq, request)
        
        // then
        assertThat(result).isNotNull()
        
        val createdMemberQuote = memberQuoteRepository.findById(result).get()
        assertThat(createdMemberQuote.member).isEqualTo(member)
        assertThat(createdMemberQuote.dailyQuote).isEqualTo(dailyQuote)
        assertThat(createdMemberQuote.likeYn).isEqualTo(request.likeYn)
    }
    
    @Test
    fun `MemberQuote 생성 성공`() {
        // given
        val member = memberPersistFactory.createMember()
        val (quote, dailyQuote) = quotePersistFactory.createQuoteWithDailyQuote()
        val memberQuote = QuoteEntityFactory.memberQuote(
            member = member,
            dailyQuote = dailyQuote
        )
        
        // when
        val result = sut.createMemberQuote(memberQuote)
        
        // then
        assertThat(result).isNotNull()
        assertThat(result.member).isEqualTo(member)
        assertThat(result.dailyQuote).isEqualTo(dailyQuote)
        
        val savedMemberQuote = memberQuoteRepository.findById(result.memberQuoteSeq).get()
        assertThat(savedMemberQuote.member).isEqualTo(member)
        assertThat(savedMemberQuote.dailyQuote).isEqualTo(dailyQuote)
    }
} 