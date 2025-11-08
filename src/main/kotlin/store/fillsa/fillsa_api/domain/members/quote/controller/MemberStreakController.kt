package store.fillsa.fillsa_api.domain.members.quote.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.dto.MemberStreakResponse
import store.fillsa.fillsa_api.domain.members.quote.service.MemberStreakService
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/member-streaks")
@Tag(name = "(회원) 연속 필사")
class MemberStreakController(
    private val memberStreakService: MemberStreakService
) {
    @GetMapping
    @Operation(summary = "[앱/위젯] 연속 필사 일수 조회 API")
    fun getStreak(
        @AuthenticationPrincipal member: Member
    ): ResponseEntity<MemberStreakResponse> = ResponseEntity.ok(
        memberStreakService.getStreak(member)
    )
}