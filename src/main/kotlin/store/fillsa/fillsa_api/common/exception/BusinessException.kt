package store.fillsa.fillsa_api.common.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

open class BusinessException(
    val status: HttpStatus,
    override val message: String?
): RuntimeException(message)

class NotFoundException(message: String):
    BusinessException(HttpStatus.NOT_FOUND, message)

class InvalidRequestException(message: String):
    BusinessException(HttpStatus.BAD_REQUEST, message)

class OAuthLoginException(message: String) :
    BusinessException(HttpStatus.UNAUTHORIZED, message)

class OAuthWithdrawalException(message: String) :
    BusinessException(HttpStatus.UNAUTHORIZED, message)