package store.fillsa.fillsa_api.common.exception

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorResponses(vararg val values: ErrorCode)
