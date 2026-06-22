package com.fillsa.app.api.oauth.client.token

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import com.fillsa.app.api.oauth.client.token.useCase.OAuthTokenClient

abstract class OAuthTokenWebClient(
    private val webClient: WebClient,
    private val clientId: String,
    private val clientSecret: String,
    private val redirectUri: String,
    private val tokenUri: String
): OAuthTokenClient {
    val log = KotlinLogging.logger {  }

    override fun getAccessToken(code: String): String {
        val request = BodyInserters.fromFormData(
            OAuthTokenClient.OAuthTokenRequest(clientId, clientSecret, redirectUri, code)
                .toMultiValueMap()
        )

        return webClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono<String>()
                    .defaultIfEmpty(StringUtils.EMPTY)
                    .flatMap {
                        log.error { "${getOAuthProvider()} 토큰 요청 실패: ${resp.statusCode()} - $it" }
                        Mono.error(
                            BusinessException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED, "${getOAuthProvider()} 토큰 요청 실패"))
                    }
            }
            .bodyToMono<JsonNode>()
            .map { it.get("access_token").asText() }
            .block() ?: throw BusinessException(ErrorCode.OAUTH_TOKEN_RESPONSE_PROCESS_FAILED, "${getOAuthProvider()} 토큰 응답 실패")
    }
}