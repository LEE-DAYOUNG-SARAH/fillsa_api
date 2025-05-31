package store.fillsa.fillsa_api.domain.oauth.client.user

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.domain.oauth.client.user.useCase.OAuthUserClient

abstract class OAuthUserWebClient(
    private val webClient: WebClient,
    private val userInfoUri: String,
): OAuthUserClient {
    val log = KotlinLogging.logger {  }

    override fun getOAuthId(accessToken: String): String {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }

        return webClient.get()
            .uri(userInfoUri)
            .headers { it.addAll(headers) }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono<String>()
                    .flatMap {
                        log.error { "${getOAuthProvider()} 사용자 정보 요청 실패: ${resp.statusCode()} - $it" }
                        Mono.error(
                            BusinessException(ErrorCode.OAUTH_USER_REQUEST_FAILED, "${getOAuthProvider()} 사용자 정보 요청 실패"))
                    }
            }
            .bodyToMono<JsonNode>()
            .map { it.get("id").asText() }
            .block() ?: throw BusinessException(ErrorCode.OAUTH_USER_RESPONSE_PROCESS_FAILED, "${getOAuthProvider()} 사용자 정보 응답 실패")
    }

}