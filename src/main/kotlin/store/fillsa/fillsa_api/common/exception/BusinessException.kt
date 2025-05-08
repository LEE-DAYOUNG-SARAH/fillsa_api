package store.fillsa.fillsa_api.common.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.message
): RuntimeException(message)