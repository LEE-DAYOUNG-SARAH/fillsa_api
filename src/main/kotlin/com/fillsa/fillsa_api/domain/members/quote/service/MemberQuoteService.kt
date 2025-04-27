package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.MemberQuoteListRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.MemberQuoteListResponse
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteUseCase
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
class MemberQuoteService(): MemberQuoteUseCase {

    override fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long {
        // TODO. 사용자 명언 조회
        // TODO. 사용자 명언 생성 or 수정
        // TODO. 사용자 명언 seq 반환
        return 1L
    }

    override fun uploadImage(member: Member, dailyQuoteSeq: Long, image: MultipartFile): Long {
        // TODO. 사용자 명언 조회
        // TODO. 사용자 명언 생성 or 수정
        // TODO. S3 이미지 업로드 및 수정
        // TODO. 사용자 명언 수정
        // TODO. 사용자 명언 seq 반환
        return 1L
    }

    override fun deleteImage(member: Member, dailyQuoteSeq: Long): Long {
        // TODO. 사용자 명언 조회
        // TODO. S3 이미지 삭제
        // TODO. 사용자 명언 수정
        // TODO. 사용자 명언 seq 반환
        return 1L
    }

    override fun getMemberQuotes(member: Member, pageable: Pageable, request: MemberQuoteListRequest): PageResponse<MemberQuoteListResponse> {
        // TODO. request 에 맞춰 사용자 명언 조회
        return PageResponse<MemberQuoteListResponse>(
            content = listOf(
                MemberQuoteListResponse(
                    memberQuoteSeq = 1L,
                    quoteDate = LocalDate.of(2025, 1,1),
                    quoteDayOfWeek = "월",
                    quote = "시간은 환상이다.",
                    author = "알베르트 아인슈타인",
                    authorUrl = "https://ko.wikipedia.org/wiki/알베르트 아인슈타인",
                    memo = "이러이러해서 메모를 적습니다.",
                    memoYn = "Y",
                    likeYn = "Y"
                ),
                MemberQuoteListResponse(
                    memberQuoteSeq = 2L,
                    quoteDate = LocalDate.of(2025, 1,2),
                    quoteDayOfWeek = "화",
                    quote = "지식은 말하지만, 지혜는 듣는다.",
                    author = "지미 헨드릭스",
                    authorUrl = null,
                    memo = "이러이러해서 메모를 적습니다.",
                    memoYn = "Y",
                    likeYn = "Y"
                )
            ),
            totalElements = 15,
            totalPages = 2,
            currentPage = 0
        )
    }

    override fun memo(member: Member, memberQuoteSeq: Long, request: MemoRequest): Long {
        // TODO. 사용자 명언 조회
        // TODO. 사용자 명언 메모 업데이트
        // TODO. 사용자 명언 seq 반환
        return memberQuoteSeq
    }
}