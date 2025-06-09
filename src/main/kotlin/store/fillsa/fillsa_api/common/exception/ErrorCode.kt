package store.fillsa.fillsa_api.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: Int,
    val message: String
) {
    // common
    NOT_FOUND(HttpStatus.NOT_FOUND, 1001, "Resource not found"),
    WITHDRAWAL_USER(HttpStatus.NOT_FOUND, 1002, "Withdrawal user"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 1003, "Invalid request"),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, 1004, "Invalid value"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1005, "Server error"),
    UNEXPECTED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 1006, "An unexpected error has occurred."),


    // oauth login
    OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, 2001, "OAuth token request failed"),
    OAUTH_TOKEN_RESPONSE_PROCESS_FAILED(HttpStatus.UNAUTHORIZED, 2002, "OAuth token response process failed"),
    OAUTH_USER_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, 2003, "OAuth user request failed"),
    OAUTH_USER_RESPONSE_PROCESS_FAILED(HttpStatus.UNAUTHORIZED, 2004, "OAuth user response process failed"),

    // auth
    JWT_ACCESS_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 3001, "Jwt access token invalid"),
    JWT_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 3002, "Jwt refresh token invalid"),
    JWT_ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 3003, "Jwt access token expired"),
    JWT_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 3004, "Jwt refresh token expired"),

    // file
    FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, 5001, "File upload failed"),
    FILE_UPDATE_FAILED(HttpStatus.BAD_REQUEST, 5002, "File update failed"),
    FILE_DELETE_FAILED(HttpStatus.BAD_REQUEST, 5003, "File delete failed"),

    // s3
    STORAGE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, 6001, "Storage upload failed"),
    STORAGE_DELETE_FAILED(HttpStatus.BAD_REQUEST, 6002, "Storage delete failed");
}