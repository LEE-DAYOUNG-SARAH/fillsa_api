package store.fillsa.fillsa_api.domain.oauth.client.token

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import store.fillsa.fillsa_api.domain.members.member.entity.Member

@Component
class GoogleTokenWebClient(
    webClient: WebClient,
    @Value("\${oauth.google.client-id}") clientId: String,
    @Value("\${oauth.google.client-secret}") clientSecret: String,
    @Value("\${oauth.google.redirect-uri}") redirectUri: String,
    @Value("\${oauth.google.token-uri}") tokenUri: String,
): OAuthTokenWebClient(webClient, clientId, clientSecret, redirectUri, tokenUri) {
    override fun getOAuthProvider() = Member.OAuthProvider.GOOGLE
}