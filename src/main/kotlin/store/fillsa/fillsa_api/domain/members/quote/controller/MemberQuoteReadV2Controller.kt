package store.fillsa.fillsa_api.domain.members.quote.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.common.dto.PageResponse
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.dto.MemberQuotesCommonRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemberQuotesRequestV2
import store.fillsa.fillsa_api.domain.members.quote.dto.MemberQuotesResponse
import store.fillsa.fillsa_api.domain.members.quote.service.MemberQuoteReadService

@RestController
@RequestMapping("/api/v2/member-quotes")
@Tag(name = "(회원) 명언 조회")
class MemberQuoteReadV2Controller(
    private val memberQuoteReadService: MemberQuoteReadService
) {

    @GetMapping
    @Operation(summary = "[4. list] V2.명언 목록 조회 api")
    fun memberQuotes(
        @AuthenticationPrincipal member: Member,
        pageable: Pageable,
        request: MemberQuotesRequestV2
    ): ResponseEntity<PageResponse<MemberQuotesResponse>> = ResponseEntity.ok(
        memberQuoteReadService.memberQuotes(member, pageable, MemberQuotesCommonRequest.fromV2(request))
    )
}