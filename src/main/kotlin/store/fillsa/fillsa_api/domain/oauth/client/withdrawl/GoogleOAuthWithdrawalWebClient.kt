package store.fillsa.fillsa_api.domain.oauth.client.withdrawl

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.client.withdrawl.useCase.GoogleOAuthWithdrawalClient

@Component
class GoogleOAuthWithdrawalWebClient(
    val webClient: WebClient,
    @Value("\${oauth.google.client-id}")
    val clientId: String,
    @Value("\${oauth.google.client-secret}")
    val clientSecret: String,
    @Value("\${oauth.google.token-uri}")
    val tokenUri: String,
    @Value("\${oauth.google.withdrawal-uri}")
    val withdrawalUri: String,
): GoogleOAuthWithdrawalClient {
    val log = KotlinLogging.logger {  }

    override fun getAccessToken(refreshToken: String): String {
        val request = BodyInserters.fromFormData("grant_type", "refresh_token")
            .with("client_id", clientId)
            .with("client_secret", clientSecret)
            .with("refresh_token", refreshToken)

        return webClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono<String>()
                    .flatMap {
                        log.error { "${getOAuthProvider()} 토큰 요청 실패: ${resp.statusCode()} - $it" }
                        Mono.error(
                            BusinessException(OAUTH_TOKEN_REQUEST_FAILED, "${getOAuthProvider()} 토큰 요청 실패"))
                    }
            }
            .bodyToMono<JsonNode>()
            .onErrorMap({ e -> e !is BusinessException }) { e ->
                log.error { "${getOAuthProvider()} 토큰 응답 처리 실패: ${e.message}" }
                BusinessException(OAUTH_TOKEN_RESPONSE_PROCESS_FAILED, "${getOAuthProvider()} 토큰 응답 처리 실패")
            }
            .map { it.path("access_token").asText() }
            .block()
            ?: throw BusinessException(OAUTH_TOKEN_RESPONSE_PROCESS_FAILED, "${getOAuthProvider()} access token 없음")
    }

    override fun withdraw(accessToken: String) {
        val response = webClient.post()
            .uri(withdrawalUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue("token=$accessToken")
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono<String>()
                    .flatMap {
                        log.error { "${getOAuthProvider()} 탈퇴 요청 실패: ${resp.statusCode()} - $it" }
                        Mono.error(
                            BusinessException(OAUTH_WITHDRAWAL_REQUEST_FAILED, "${getOAuthProvider()} 탈퇴 요청 실패"))
                    }
            }
            .bodyToMono<String>()
            .block()

        log.info { "${getOAuthProvider()} 탈퇴 응답: $response" }
    }

    override fun getOAuthProvider() = Member.OAuthProvider.GOOGLE
}