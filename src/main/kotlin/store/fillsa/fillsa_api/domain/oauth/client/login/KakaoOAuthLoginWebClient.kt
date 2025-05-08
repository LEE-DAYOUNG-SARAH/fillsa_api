package store.fillsa.fillsa_api.domain.oauth.client.login

import com.fasterxml.jackson.annotation.JsonProperty
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.client.login.useCase.*
import java.time.LocalDateTime

@Component
class KakaoOAuthLoginWebClient(
    val webClient: WebClient,
    @Value("\${oauth.kakao.client-id}")
    val clientId: String,
    @Value("\${oauth.kakao.client-secret}")
    val clientSecret: String,
    @Value("\${oauth.kakao.redirect-uri}")
    val redirectUri: String,
    @Value("\${oauth.kakao.token-uri}")
    val tokenUri: String,
    @Value("\${oauth.kakao.user-info-uri}")
    val userInfoUri: String,
): OAuthLoginClient {
    val log = KotlinLogging.logger {  }

    override fun getAccessToken(code: String): OAuthTokenInfo {
        val request = BodyInserters.fromFormData("grant_type", "authorization_code")
            .with("client_id", clientId)
            .with("client_secret", clientSecret)
            .with("redirect_uri", redirectUri)
            .with("code", code)

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
                            BusinessException(OAUTH_TOKEN_REQUEST_FAILED, "${getOAuthProvider()} 토큰 요청 실패"))
                    }
            }
            .bodyToMono<KakaoOAuthTokenResponse>()
            .onErrorMap({ e -> e !is BusinessException }) { e ->
                log.error { "${getOAuthProvider()} 토큰 응답 실패: ${e.message}" }
                BusinessException(OAUTH_TOKEN_RESPONSE_PROCESS_FAILED, "${getOAuthProvider()} 토큰 응답 실패")
            }
            .blockOptional()                       // Optional<T>
            .orElseThrow {
                BusinessException(OAUTH_TOKEN_RESPONSE_PROCESS_FAILED, "${getOAuthProvider()} 토큰 응답 없음")
            }
            .toOAuthTokenInfo()
    }

    override fun getUserInfo(accessToken: String): OAuthUserInfo {
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
                            BusinessException(OAUTH_USER_REQUEST_FAILED, "${getOAuthProvider()} 사용자 정보 요청 실패"))
                    }
            }
            .bodyToMono<KakaoOAuthUserResponse>()
            .onErrorMap({ e -> e !is BusinessException }) { e ->
                log.error { "${getOAuthProvider()} 사용자 정보 응답 실패: ${e.message}" }
                BusinessException(OAUTH_USER_RESPONSE_PROCESS_FAILED, "${getOAuthProvider()} 사용자 정보 응답 실패")
            }
            .blockOptional()                       // Optional<T>
            .orElseThrow {
                BusinessException(OAUTH_USER_RESPONSE_PROCESS_FAILED, "${getOAuthProvider()} 사용자 정보 응답 없음")
            }
            .toOAuthUserInfo()
    }

    override fun getOAuthProvider() = Member.OAuthProvider.KAKAO

    data class KakaoOAuthTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,

        @JsonProperty("expires_in")
        val expiresIn: Long,

        @JsonProperty("refresh_token")
        val refreshToken: String,

        @JsonProperty("refresh_token_expires_in")
        val refreshTokenExpiresIn: Long
    ): OAuthTokenResponse {
        override fun toOAuthTokenInfo(): OAuthTokenInfo {
            val now = LocalDateTime.now()

            return OAuthTokenInfo(
                accessToken = accessToken,
                accessTokenExpiresAt = now.plusSeconds(expiresIn),
                refreshToken = refreshToken,
                refreshTokenExpiresAt = now.plusSeconds(refreshTokenExpiresIn)
            )
        }
    }

    data class KakaoOAuthUserResponse(
        val id: String,
        val properties: KakaoProperties
    ): OAuthUserResponse {

        data class KakaoProperties(
            val nickname: String,
            @JsonProperty("thumbnail_image")
            val thumbnailImage: String?
        )

        override fun toOAuthUserInfo() = OAuthUserInfo(
            id = id,
            nickname = properties.nickname,
            profileImageUrl = properties.thumbnailImage,
            oAuthProvider = Member.OAuthProvider.KAKAO
        )
    }
}