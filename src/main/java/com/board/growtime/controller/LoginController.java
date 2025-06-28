package com.board.growtime.controller;

import com.board.growtime.entity.User;
import com.board.growtime.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<?> handleCallback(@RequestParam(required = false) String code,
                                          @RequestParam(required = false) String error) {
        
        // OAuth 에러 처리
        if (error != null) {
            log.error("GitHub OAuth 에러: {}", error);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "GitHub 로그인에 실패했습니다: " + error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // 인증 코드 검증
        if (code == null || code.trim().isEmpty()) {
            log.error("인증 코드가 없습니다");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "인증 코드가 없습니다");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            // 액세스 토큰 획득
            String accessToken = getAccessToken(code);
            if (accessToken == null) {
                log.error("액세스 토큰을 가져올 수 없습니다");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "액세스 토큰을 가져올 수 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // 사용자 정보 획득
            GitHubUserInfo userInfo = getUserInfo(accessToken);
            if (userInfo == null) {
                log.error("사용자 정보를 가져올 수 없습니다");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "사용자 정보를 가져올 수 없습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // 사용자 정보를 데이터베이스에 저장 또는 업데이트
            User savedUser = userService.saveOrUpdateUser(
                userInfo.getId().toString(),
                userInfo.getLogin(),
                userInfo.getName(),
                userInfo.getEmail(),
                userInfo.getAvatarUrl(),
                userInfo.getHtmlUrl(),
                userInfo.getCompany(),
                userInfo.getLocation(),
                userInfo.getBio(),
                accessToken
            );

            // 성공 응답
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "GitHub 로그인 성공");
            successResponse.put("user", createUserResponse(savedUser));
            successResponse.put("accessToken", accessToken); // 실제로는 JWT 토큰을 발급해야 함
            
            log.info("GitHub 로그인 성공: {} (ID: {})", userInfo.getLogin(), savedUser.getId());
            return ResponseEntity.ok(successResponse);

        } catch (RestClientException e) {
            log.error("GitHub API 호출 중 에러 발생", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "GitHub API 호출 중 에러가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            log.error("로그인 처리 중 예상치 못한 에러 발생", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인 처리 중 에러가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private String getAccessToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            OAuthAccessTokenRequest tokenRequest = new OAuthAccessTokenRequest(clientId, clientSecret, code);
            HttpEntity<OAuthAccessTokenRequest> request = new HttpEntity<>(tokenRequest, headers);

            OAuthAccessTokenResponse response = restTemplate.postForObject(
                    ACCESS_TOKEN_URL,
                    request,
                    OAuthAccessTokenResponse.class
            );

            if (response != null && response.getAccessToken() != null) {
                return response.getAccessToken();
            }
            
            log.error("액세스 토큰 응답이 null이거나 토큰이 없습니다");
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

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("githubId", user.getGithubId());
        userResponse.put("login", user.getLogin());
        userResponse.put("name", user.getName());
        userResponse.put("email", user.getEmail());
        userResponse.put("avatarUrl", user.getAvatarUrl());
        userResponse.put("htmlUrl", user.getHtmlUrl());
        userResponse.put("company", user.getCompany());
        userResponse.put("location", user.getLocation());
        userResponse.put("bio", user.getBio());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        return userResponse;
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
