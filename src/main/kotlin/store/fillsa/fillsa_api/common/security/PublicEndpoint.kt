package store.fillsa.fillsa_api.common.security

enum class PublicEndpoint(val patterns: List<String>) {
    SWAGGER(listOf(
        "/swagger-ui/**",
        "/v3/api-docs/**"
    )),
    AUTH(listOf(
        "/auth/login",
        "/auth/refresh"
    )),
    OAUTH(listOf(
        "/oauth/**"
    )),
    QUOTES(listOf(
        "/quotes/**"
    )),
    NOTICES(listOf(
        "/notices"
    )),
    ACTUATOR(listOf(
        "/actuator/**"
    ));

    companion object {
        fun allPublicPatterns(): Array<String> =
            entries.flatMap { it.patterns }.toTypedArray()
    }
}

