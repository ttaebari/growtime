package com.board.growtime.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String githubId;

    @Column(nullable = false)
    private String login;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String avatarUrl;

    @Column
    private String htmlUrl;

    @Column
    private String company;

    @Column
    private String location;

    @Column
    private String bio;

    @Column
    private String accessToken;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 생성자
    public User(String githubId, String login, String name, String email, 
                String avatarUrl, String htmlUrl, String company, String location, String bio) {
        this.githubId = githubId;
        this.login = login;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.htmlUrl = htmlUrl;
        this.company = company;
        this.location = location;
        this.bio = bio;
    }

    // 사용자 정보 업데이트 메서드
    public void updateUserInfo(String name, String email, String avatarUrl, 
                              String htmlUrl, String company, String location, String bio) {
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.htmlUrl = htmlUrl;
        this.company = company;
        this.location = location;
        this.bio = bio;
    }

    // 액세스 토큰 업데이트
    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
} 