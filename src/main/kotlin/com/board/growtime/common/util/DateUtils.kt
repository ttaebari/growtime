package com.board.growtime.common.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 날짜 관련 유틸리티 클래스
 * 
 * 학습 포인트:
 * 1. Utility Class 패턴 - 정적 메서드로 구성된 유틸리티 클래스
 * 2. 단일 책임 원칙 - 날짜 관련 로직만 담당
 * 3. 재사용성 - 여러 서비스에서 공통으로 사용 가능
 */
object DateUtils {
    
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    /**
     * 문자열을 LocalDate로 변환
     */
    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }
    
    /**
     * 날짜 유효성 검증
     */
    fun isValidDate(dateString: String): Boolean {
        return parseDate(dateString) != null
    }
    
    /**
     * 두 날짜 사이의 일수 계산
     */
    fun calculateDaysBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)
    }
    
    /**
     * 복무 기간 검증 (산업기능요원 기준)
     */
    fun isValidServicePeriod(entryDate: LocalDate, dischargeDate: LocalDate): Boolean {
        val days = calculateDaysBetween(entryDate, dischargeDate)
        return days in 365..1500 // 1년 ~ 4년
    }
    
    /**
     * 복무 진행률 계산
     */
    fun calculateServiceProgress(entryDate: LocalDate, dischargeDate: LocalDate): Double {
        val today = LocalDate.now()
        val totalDays = calculateDaysBetween(entryDate, dischargeDate)
        val passedDays = calculateDaysBetween(entryDate, today)
        
        if (totalDays <= 0) return 0.0
        if (passedDays < 0) return 0.0
        if (passedDays > totalDays) return 100.0
        
        return (passedDays.toDouble() / totalDays * 100).coerceIn(0.0, 100.0)
    }
}
