package store.fillsa.fillsa_api.domain.oauth.client.withdrawl

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import store.fillsa.fillsa_api.common.exception.ErrorCode.OAUTH_WITHDRAWAL_REQUEST_FAILED
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.client.withdrawl.useCase.KakaoOAuthWithdrawalClient

@Component
class KakaoOauthWithdrawalWebClient(
    val webClient: WebClient,
    @Value("\${oauth.kakao.admin-key}")
    val adminKey: String,
    @Value("\${oauth.kakao.withdrawal-uri}")
    val withdrawalUri: String,
): KakaoOAuthWithdrawalClient {
    val log = KotlinLogging.logger {  }

    override fun withdraw(oauthId: String) {
        val headers = HttpHeaders().apply {
            setBearerAuth("KakaoAK $adminKey")
        }
        val request = BodyInserters.fromFormData("target_id_type", "user_id")
            .with("target_id", oauthId)

        val response = webClient.post()
            .uri(withdrawalUri)
            .headers { it.addAll(headers) }
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(request)
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

    override fun getOAuthProvider() = Member.OAuthProvider.KAKAO
}