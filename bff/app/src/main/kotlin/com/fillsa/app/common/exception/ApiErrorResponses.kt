package com.fillsa.app.common.exception
import com.fillsa.util.exception.ErrorCode

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorResponses(vararg val values: ErrorCode)
