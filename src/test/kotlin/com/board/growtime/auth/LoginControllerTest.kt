package com.board.growtime.auth

import com.board.growtime.user.GitHubUserService
import com.board.growtime.user.User
import com.board.growtime.user.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate

class LoginControllerTest {

    private val jwtService = JwtService(
        configuredSecret = "test-secret-test-secret-test-secret",
        expirationSeconds = 3600
    )
    private lateinit var restTemplate: RestTemplate
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var userRepository: UserRepository
    private lateinit var controller: LoginController
    private lateinit var savedUsers: MutableList<User>

    @BeforeEach
    fun setUp() {
        restTemplate = RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        userRepository = Mockito.mock(UserRepository::class.java)
        savedUsers = mutableListOf()

        Mockito.`when`(userRepository.save(ArgumentMatchers.any(User::class.java))).thenAnswer { invocation ->
            val user = invocation.getArgument<User>(0)
            savedUsers.add(user)
            user
        }

        controller = LoginController(
            restTemplate = restTemplate,
            gitHubUserService = GitHubUserService(userRepository),
            jwtService = jwtService,
            clientId = "github-client-id",
            clientSecret = "github-client-secret"
        )
    }

    @Test
    fun `callback returns app jwt on github oauth success`() {
        mockServer.expect(requestTo("https://github.com/login/oauth/access_token"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""{"access_token":"github-access-token"}""", MediaType.APPLICATION_JSON))
        mockServer.expect(requestTo("https://api.github.com/user"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""{"id":118417552,"login":"ttaebari","name":"Taeho"}""", MediaType.APPLICATION_JSON))

        val response = controller.handleCallback("oauth-code", null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val loginResponse = response.body!!.data!!
        assertThat(loginResponse.githubId).isEqualTo("118417552")
        assertThat(loginResponse.refreshToken).isNull()
        assertThat(jwtService.parseToken(loginResponse.accessToken).githubId).isEqualTo("118417552")
        assertThat(savedUsers.single().accessToken).isNull()
        mockServer.verify()
    }

    @Test
    fun `callback returns 400 when oauth code is missing`() {
        val response = controller.handleCallback(null, null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.error!!.code).isEqualTo("NO_AUTH_CODE")
    }

    @Test
    fun `callback returns 400 when github sends oauth error`() {
        val response = controller.handleCallback(null, "redirect_uri_mismatch")

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.error!!.code).isEqualTo("OAUTH_ERROR")
    }

    @Test
    fun `callback returns 502 when github token exchange fails`() {
        mockServer.expect(requestTo("https://github.com/login/oauth/access_token"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY))

        val response = controller.handleCallback("oauth-code", null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_GATEWAY)
        assertThat(response.body!!.error!!.code).isEqualTo("GITHUB_TOKEN_EXCHANGE_FAILED")
        mockServer.verify()
    }
}
