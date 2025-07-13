package com.board.growtime.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
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
    private String avatarUrl;

    @Column
    private String htmlUrl;

    @Column
    private String location;

    @Column
    private String accessToken;

    @Column
    private LocalDate entryDate; // 입영날짜

    @Column
    private LocalDate dischargeDate; // 제대날짜

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 생성자
    public User(String githubId, String login, String name,
                String avatarUrl, String htmlUrl, String location) {
        this.githubId = githubId;
        this.login = login;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.htmlUrl = htmlUrl;
        this.location = location;
    }

    // 사용자 정보 업데이트 메서드
    public void updateUserInfo(String name, String avatarUrl,
                              String htmlUrl, String location) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.htmlUrl = htmlUrl;
        this.location = location;
    }

    // 액세스 토큰 업데이트
    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    // 입영/제대 날짜 설정
    public void setServiceDates(LocalDate entryDate, LocalDate dischargeDate) {
        this.entryDate = entryDate;
        this.dischargeDate = dischargeDate;
    }

    // D-day 계산
    public long calculateDDay() {
        if (dischargeDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(today, dischargeDate);
    }

    // 복무 시작일로부터 경과일 계산
    public long calculateServiceDays() {
        if (entryDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(entryDate, today);
    }

    // 전체 복무기간 계산
    public long calculateTotalServiceDays() {
        if (entryDate == null || dischargeDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(entryDate, dischargeDate);
    }
} 