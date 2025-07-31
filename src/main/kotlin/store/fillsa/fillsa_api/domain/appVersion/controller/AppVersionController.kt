package store.fillsa.fillsa_api.domain.appVersion.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.domain.appVersion.dto.AppVersionModifyRequest
import store.fillsa.fillsa_api.domain.appVersion.dto.AppVersionResponse
import store.fillsa.fillsa_api.domain.appVersion.service.AppVersionService

@RestController
@RequestMapping("/api/v1/app-versions")
@Tag(name = "앱 서버")
class AppVersionController(
    private val appVersionService: AppVersionService
) {

    @GetMapping
    @Operation(summary = "[개발자용] 앱 버전정보 조회 api")
    fun appVersion(): ResponseEntity<AppVersionResponse> = ResponseEntity.ok(
        appVersionService.getAppVersion()
    )

    @PutMapping
    @Operation(summary = "[개발자용] 앱 버전정보 수정 api")
    fun modifyAppVersion(
        @RequestBody request: AppVersionModifyRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(
        appVersionService.modify(request)
    )
}