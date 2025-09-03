package store.fillsa.fillsa_api.domain.test

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode

@RestController
@RequestMapping("/test")
class ErrorCodeTestController {

    @GetMapping("/code/{code}")
    @Operation(summary = "[개발자용] 앱 에러코드 케이스별 테스트 api")
    fun errorCode(
        @PathVariable @Parameter(description = "ErrorCode의 code") code: Int
    ) {
        val errorCode = ErrorCode.entries.find { it.code == code }
            ?: throw BusinessException(ErrorCode.INVALID_REQUEST)

        throw BusinessException(errorCode)
    }

}