package com.board.growtime.controller;

import com.board.growtime.entity.User;
import com.board.growtime.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final RestTemplate restTemplate;
    private final UserService userService;

    @Value("${github.clientId}")
    private String clientId;

    @Value("${github.clientSecret}")
    private String clientSecret;

    private static final String ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String MEMBER_INFO_URL = "https://api.github.com/user";
    private static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        String authUrl = String.format("%s?client_id=%s&scope=read:user,user:email", 
                                     GITHUB_AUTH_URL, clientId);
        
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        response.put("message", "GitHub 로그인을 위해 위 URL로 리다이렉트하세요");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    public void handleCallback(@RequestParam(required = false) String code,
                              @RequestParam(required = false) String error,
                              HttpServletResponse response) throws java.io.IOException {
        log.info("GitHub 콜백 호출됨 - code: {}, error: {}", code, error);
        
        // OAuth 에러 처리
        if (error != null) {
            log.error("GitHub OAuth 에러: {}", error);
            response.sendRedirect("http://localhost:3000/?error=" + error);
            return;
        }
        if (code == null || code.trim().isEmpty()) {
            log.error("인증 코드가 없습니다");
            response.sendRedirect("http://localhost:3000/?error=no_auth_code");
            return;
        }
        try {
            log.info("액세스 토큰 요청 시작");
            String accessToken = getAccessToken(code);
            if (accessToken == null) {
                log.error("액세스 토큰을 받지 못했습니다");
                response.sendRedirect("http://localhost:3000/?error=no_token");
                return;
            }
            log.info("액세스 토큰 획득 성공");
            
            log.info("사용자 정보 요청 시작");
            GitHubUserInfo userInfo = getUserInfo(accessToken);
            if (userInfo == null) {
                log.error("사용자 정보를 받지 못했습니다");
                response.sendRedirect("http://localhost:3000/?error=no_user_info");
                return;
            }
            log.info("사용자 정보 획득 성공: {}", userInfo.getLogin());
            // 사용자 정보를 데이터베이스에 저장 또는 업데이트
            log.info("사용자 정보 저장 시작");
            User savedUser = userService.saveOrUpdateUser(
                userInfo.getId().toString(),
                userInfo.getLogin(),
                userInfo.getName(),
                userInfo.getAvatarUrl(),
                userInfo.getHtmlUrl(),
                userInfo.getLocation(),
                accessToken
            );
            log.info("사용자 정보 저장 완료: {}", savedUser.getGithubId());
            
            // 프론트엔드 메인페이지로 리다이렉트
            String redirectUrl = "http://localhost:3000/main?githubId=" + savedUser.getGithubId();
            log.info("리다이렉트 URL: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("콜백 처리 중 예외 발생", e);
            response.sendRedirect("http://localhost:3000/?error=server_error");
        }
    }

    private String getAccessToken(String code) {
        try {
            log.info("액세스 토큰 요청 - clientId: {}, code: {}", clientId, code);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            OAuthAccessTokenRequest tokenRequest = new OAuthAccessTokenRequest(clientId, clientSecret, code);
            HttpEntity<OAuthAccessTokenRequest> request = new HttpEntity<>(tokenRequest, headers);

            log.info("GitHub API 호출: {}", ACCESS_TOKEN_URL);
            OAuthAccessTokenResponse response = restTemplate.postForObject(
                    ACCESS_TOKEN_URL,
                    request,
                    OAuthAccessTokenResponse.class
            );

            if (response != null && response.getAccessToken() != null) {
                log.info("액세스 토큰 획득 성공");
                return response.getAccessToken();
            }
            
            if (response != null) {
                log.error("액세스 토큰 응답 에러: {}, {}", response.getError(), response.getErrorDescription());
            } else {
                log.error("액세스 토큰 응답이 null입니다");
            }
            return null;
            
        } catch (Exception e) {
            log.error("액세스 토큰 획득 중 에러 발생", e);
            return null;
        }
    }

    private GitHubUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<GitHubUserInfo> response = restTemplate.exchange(
                    MEMBER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    GitHubUserInfo.class
            );

            return response.getBody();
            
        } catch (Exception e) {
            log.error("사용자 정보 획득 중 에러 발생", e);
            return null;
        }
    }

    @NoArgsConstructor
    @Getter
    public static class OAuthAccessTokenRequest {
        @JsonProperty("client_id")
        private String clientId;

        @JsonProperty("client_secret")
        private String clientSecret;

        @JsonProperty("code")
        private String code;

        public OAuthAccessTokenRequest(String clientId, String clientSecret, String code) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.code = code;
        }
    }

    @NoArgsConstructor
    @Getter
    public static class OAuthAccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("error")
        private String error;
        
        @JsonProperty("error_description")
        private String errorDescription;
    }

    @NoArgsConstructor
    @Getter
    public static class GitHubUserInfo {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("login")
        private String login;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("avatar_url")
        private String avatarUrl;
        
        @JsonProperty("html_url")
        private String htmlUrl;
        
        @JsonProperty("company")
        private String company;
        
        @JsonProperty("location")
        private String location;
        
        @JsonProperty("bio")
        private String bio;
    }
}
