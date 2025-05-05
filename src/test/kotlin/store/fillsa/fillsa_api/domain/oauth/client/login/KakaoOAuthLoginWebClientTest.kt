package store.fillsa.fillsa_api.domain.oauth.client.login

import store.fillsa.fillsa_api.common.exception.OAuthLoginException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class KakaoOAuthLoginWebClientTest {
    lateinit var mockWebServer: MockWebServer
    lateinit var sut: KakaoOAuthLoginWebClient

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer().apply { start() }

        val baseUrl = mockWebServer.url("/").toString()
        val webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build()

        sut = KakaoOAuthLoginWebClient(
            webClient     = webClient,
            clientId      = "",
            clientSecret  = "",
            redirectUri   = "",
            tokenUri      = baseUrl,
            userInfoUri   = baseUrl
        )
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `카카오 토큰 발급 성공`() {
        // given
        val jsonBody = """
          {
            "access_token": "ATOK",
            "token_type": "Bearer",
            "refresh_token": "RTOK",
            "expires_in": 3600,
            "refresh_token_expires_in": 7200
          }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonBody)
        )

        // when
        val accessToken = sut.getAccessToken("dummy-code")

        // then
        assertThat(accessToken).isEqualTo("ATOK")
    }

    @Test
    fun `카카오 토큰 발급 실패 - 요청 실패`() {
        // given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""{"error":"bad_request"}""")
        )

        // when & then
        assertThatThrownBy { sut.getAccessToken("dummy-code") }
            .isInstanceOf(OAuthLoginException::class.java)
            .hasMessage("KAKAO 토큰 요청 실패")
    }

    @Test
    fun `카카오 토큰 발급 실패 - 응답 실패`() {
        // given
        val jsonBody = """
          {}
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonBody)
        )

        // when & then
        assertThatThrownBy { sut.getAccessToken("dummy-code") }
            .isInstanceOf(OAuthLoginException::class.java)
            .hasMessage("KAKAO 토큰 응답 실패")
    }

    @Test
    fun `카카오 사용자 조회 성공`() {
        // given
        val jsonBody = """
          {
            "id": "ID",
            "properties": {
                "nickname": "NICKNAME",
                "thumbnail_image": null
            }
          }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonBody)
        )

        // when
        val userInfo = sut.getUserInfo("dummy-token")

        // then
        assertThat(userInfo.id).isEqualTo("ID")
        assertThat(userInfo.nickname).isEqualTo("NICKNAME")
        assertThat(userInfo.profileImageUrl).isNull()
    }

    @Test
    fun `카카오 사용자 조회 실패 - 요청 상태 실패`() {
        // given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""{"error":"bad_request"}""")
        )

        // when & then
        assertThatThrownBy { sut.getUserInfo("dummy-token") }
            .isInstanceOf(OAuthLoginException::class.java)
            .hasMessage("KAKAO 사용자 정보 요청 실패")
    }

    @Test
    fun `카카오 사용자 조회 실패 - 응답 데이터 실패`() {
        // given
        val jsonBody = """
          {}
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonBody)
        )

        // when & then
        assertThatThrownBy { sut.getUserInfo("dummy-token") }
            .isInstanceOf(OAuthLoginException::class.java)
            .hasMessage("KAKAO 사용자 정보 응답 실패")
    }

}