package com.board.growtime.user

import com.board.growtime.core.BaseEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Column(unique = true, nullable = false)
    var githubId: String,
    
    @Column(nullable = false)
    var login: String,
    
    @Column
    var name: String? = null,
    
    @Column
    var avatarUrl: String? = null,
    
    @Column
    var htmlUrl: String? = null,
    
    @Column
    var location: String? = null,
    
    @Column
    var accessToken: String? = null,
    
    @Column
    var entryDate: LocalDate? = null, // 입영날짜
    
    @Column
    var dischargeDate: LocalDate? = null, // 제대날짜
) : BaseEntity(){
    // 기본 생성자 (JPA용)
    protected constructor() : this("", "")

    // 사용자 정보 업데이트 메서드
    fun updateUserInfo(name: String?, avatarUrl: String?, htmlUrl: String?, location: String?) {
        this.name = name
        this.avatarUrl = avatarUrl
        this.htmlUrl = htmlUrl
        this.location = location
    }

    // 액세스 토큰 업데이트
    fun updateAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }

    // 입영/제대 날짜 설정
    fun setServiceDates(entryDate: LocalDate, dischargeDate: LocalDate) {
        this.entryDate = entryDate
        this.dischargeDate = dischargeDate
    }

    // D-day 계산
    fun calculateDDay(): Long {
        return dischargeDate?.let { 
            ChronoUnit.DAYS.between(LocalDate.now(), it) 
        } ?: 0
    }

    // 복무 시작일로부터 경과일 계산
    fun calculateServiceDays(): Long {
        return entryDate?.let { 
            ChronoUnit.DAYS.between(it, LocalDate.now()) 
        } ?: 0
    }

    // 전체 복무기간 계산
    fun calculateTotalServiceDays(): Long {
        return if (entryDate != null && dischargeDate != null) {
            ChronoUnit.DAYS.between(entryDate, dischargeDate)
        } else {
            0
        }
    }
} 