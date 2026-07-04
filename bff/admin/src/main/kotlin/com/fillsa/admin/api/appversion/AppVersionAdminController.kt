package com.fillsa.admin.api.appversion

import com.fillsa.admin.service.appversion.AppVersionAdminService
import com.fillsa.admin.service.appversion.AppVersionModifyRequest
import com.fillsa.admin.service.appversion.AppVersionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/v1/app-version")
@Tag(name = "app-version", description = "앱 버전")
class AppVersionAdminController(
    private val appVersionAdminService: AppVersionAdminService,
) {

    @GetMapping
    @Operation(summary = "버전 정보 조회")
    fun getAppVersion(): ResponseEntity<AppVersionResponse> =
        ResponseEntity.ok(appVersionAdminService.get())

    @PutMapping
    @Operation(summary = "버전 정보 수정 (blank 필드는 무시)")
    fun updateAppVersion(@RequestBody request: AppVersionModifyRequest): ResponseEntity<AppVersionResponse> =
        ResponseEntity.ok(appVersionAdminService.update(request))
}
