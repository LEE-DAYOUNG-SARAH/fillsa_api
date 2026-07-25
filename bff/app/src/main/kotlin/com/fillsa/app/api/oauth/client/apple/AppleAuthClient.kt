package com.fillsa.app.api.oauth.client.apple

import com.fasterxml.jackson.databind.JsonNode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Date

/**
 * Sign in with Apple 토큰 폐기(revoke) 클라이언트.
 *
 * 앱스토어 심사 요건: 계정 삭제 시 Apple 토큰을 서버가 폐기해야 한다.
 * 로그인 자체는 카카오/구글과 동일하게 앱이 수행하고 서버는 관여하지 않는다 —
 * 이 클라이언트는 "앱 내 탈퇴" 시 앱이 전달한 authorizationCode 처리 전용.
 *
 * 흐름: p8 키로 client_secret(ES256 JWT) 생성 → /auth/token 교환 → /auth/revoke
 * 설정(oauth.apple.*)이 비어 있으면 호출 시점에만 실패한다 (부팅에는 영향 없음).
 */
@Component
class AppleAuthClient(
    private val webClient: WebClient,
    @Value("\${oauth.apple.team-id:}") private val teamId: String,
    @Value("\${oauth.apple.key-id:}") private val keyId: String,
    @Value("\${oauth.apple.client-id:}") private val clientId: String,
    @Value("\${oauth.apple.private-key:}") private val privateKeyPem: String,
    @Value("\${oauth.apple.token-uri:https://appleid.apple.com/auth/token}") private val tokenUri: String,
    @Value("\${oauth.apple.revoke-uri:https://appleid.apple.com/auth/revoke}") private val revokeUri: String,
) {
    private val log = KotlinLogging.logger { }

    /** 탈퇴 시 호출: authorizationCode 를 토큰으로 교환한 뒤 refresh token 을 폐기한다. */
    fun revoke(authorizationCode: String) {
        val clientSecret = generateClientSecret()

        val refreshToken = exchangeCode(authorizationCode, clientSecret)
        revokeToken(refreshToken, clientSecret)
        log.info { "Apple 토큰 폐기 완료" }
    }

    private fun exchangeCode(code: String, clientSecret: String): String {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("code", code)
            add("grant_type", "authorization_code")
        }

        return webClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromFormData(form))
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono<String>()
                    .defaultIfEmpty(StringUtils.EMPTY)
                    .flatMap {
                        log.error { "APPLE 토큰 요청 실패: ${resp.statusCode()} - $it" }
                        Mono.error(BusinessException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED, "APPLE 토큰 요청 실패"))
                    }
            }
            .bodyToMono<JsonNode>()
            .map { it.get("refresh_token")?.asText() ?: it.get("access_token").asText() }
            .block() ?: throw BusinessException(ErrorCode.OAUTH_TOKEN_RESPONSE_PROCESS_FAILED, "APPLE 토큰 응답 실패")
    }

    private fun revokeToken(token: String, clientSecret: String) {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("token", token)
            add("token_type_hint", "refresh_token")
        }

        webClient.post()
            .uri(revokeUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(form))
            .retrieve()
            .onStatus({ it.isError }) { resp ->
                resp.bodyToMono<String>()
                    .defaultIfEmpty(StringUtils.EMPTY)
                    .flatMap {
                        log.error { "APPLE 토큰 폐기 실패: ${resp.statusCode()} - $it" }
                        Mono.error(BusinessException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED, "APPLE 토큰 폐기 실패"))
                    }
            }
            .toBodilessEntity()
            .block()
    }

    /** Apple 규격 client_secret: p8(EC P-256) 키로 서명한 ES256 JWT (유효기간 최대 6개월, 여기선 5분). */
    internal fun generateClientSecret(): String {
        if (teamId.isBlank() || keyId.isBlank() || clientId.isBlank() || privateKeyPem.isBlank()) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "Apple OAuth 설정(oauth.apple.*)이 비어 있습니다")
        }
        val now = Date()
        return Jwts.builder()
            .setHeaderParam("kid", keyId)
            .setIssuer(teamId)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + 5 * 60 * 1000))
            .setAudience("https://appleid.apple.com")
            .setSubject(clientId)
            .signWith(parsePrivateKey(privateKeyPem), SignatureAlgorithm.ES256)
            .compact()
    }

    /** p8 내용(PEM 헤더 포함/미포함 모두 허용)을 EC PrivateKey 로 파싱. */
    private fun parsePrivateKey(pem: String): PrivateKey {
        val base64 = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(base64)
        return KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
    }
}
