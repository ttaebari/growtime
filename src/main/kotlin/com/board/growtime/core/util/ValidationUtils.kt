package com.board.growtime.core.util

/**
 * 입력값 검증 유틸리티 클래스
 * 
 * 학습 포인트:
 * 1. Validation Utility 패턴 - 공통 검증 로직을 한 곳에서 관리
 * 2. 비즈니스 규칙 집중 - 도메인별 검증 규칙을 명확히 정의
 * 3. 재사용성 - 여러 서비스에서 동일한 검증 로직 사용
 */
object ValidationUtils {
    
    /**
     * 문자열이 비어있거나 공백만 있는지 확인
     */
    fun isBlank(value: String?): Boolean {
        return value == null || value.trim().isEmpty()
    }
    
    /**
     * 문자열 길이 검증
     */
    fun isValidLength(value: String, minLength: Int, maxLength: Int): Boolean {
        return value.length in minLength..maxLength
    }
    
    /**
     * GitHub ID 검증
     */
    fun isValidGitHubId(githubId: String?): Boolean {
        if (isBlank(githubId)) return false
        val trimmed = githubId!!.trim()
        return trimmed.length in 1..39 && trimmed.matches(Regex("[a-zA-Z0-9-]+"))
    }
    
    /**
     * 회고 제목 검증
     */
    fun isValidNoteTitle(title: String?): Boolean {
        if (isBlank(title)) return false
        val trimmed = title!!.trim()
        return isValidLength(trimmed, 1, 100)
    }
    
    /**
     * 회고 내용 검증
     */
    fun isValidNoteContent(content: String?): Boolean {
        if (isBlank(content)) return false
        val trimmed = content!!.trim()
        return isValidLength(trimmed, 1, 5000)
    }
    
    /**
     * 이메일 형식 검증 (간단한 버전)
     */
    fun isValidEmail(email: String?): Boolean {
        if (isBlank(email)) return false
        return email!!.contains("@") && email.contains(".")
    }
    
    /**
     * URL 형식 검증 (간단한 버전)
     */
    fun isValidUrl(url: String?): Boolean {
        if (isBlank(url)) return false
        return url!!.startsWith("http://") || url.startsWith("https://")
    }
}
