package com.board.growtime.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthAccessTokenRequest(
    @JsonProperty("client_id")
    val clientId: String,

    @JsonProperty("client_secret")
    val clientSecret: String,

    @JsonProperty("code")
    val code: String
)

data class OAuthAccessTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String? = null,

    @JsonProperty("error")
    val error: String? = null,

    @JsonProperty("error_description")
    val errorDescription: String? = null
)

data class GitHubUserInfo(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("login")
    val login: String,

    @JsonProperty("name")
    val name: String? = null,

    @JsonProperty("email")
    val email: String? = null,

    @JsonProperty("avatar_url")
    val avatarUrl: String? = null,

    @JsonProperty("html_url")
    val htmlUrl: String? = null,

    @JsonProperty("company")
    val company: String? = null,

    @JsonProperty("location")
    val location: String? = null,

    @JsonProperty("bio")
    val bio: String? = null
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String?,
    val githubId: String
)
