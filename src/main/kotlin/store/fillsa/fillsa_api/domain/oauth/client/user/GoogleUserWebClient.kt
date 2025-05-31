package store.fillsa.fillsa_api.domain.oauth.client.user

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import store.fillsa.fillsa_api.domain.members.member.entity.Member

@Component
class GoogleUserWebClient(
    webClient: WebClient,
    @Value("\${oauth.google.user-info-uri}") userInfoUri: String,
): OAuthUserWebClient(webClient, userInfoUri) {
    override fun getOAuthProvider() = Member.OAuthProvider.GOOGLE
}