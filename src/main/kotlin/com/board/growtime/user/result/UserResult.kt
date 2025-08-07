package com.board.growtime.user.result

import com.board.growtime.user.User
import com.board.growtime.user.dto.DDayInfo
import com.board.growtime.user.dto.UserInfo

/**
 * 사용자 정보 조회 결과
 */
sealed class UserInfoResult {
    data class Success(val userInfo: UserInfo) : UserInfoResult()
    object UserNotFound : UserInfoResult()
}

/**
 * 복무 날짜 설정 결과
 */
sealed class ServiceDateResult {
    data class Success(val user: User) : ServiceDateResult()
    object UserNotFound : ServiceDateResult()
    data class InvalidDates(val message: String) : ServiceDateResult()
}

/**
 * D-day 정보 조회 결과
 */
sealed class DDayInfoResult {
    data class Success(val dDayInfo: DDayInfo) : DDayInfoResult()
    object UserNotFound : DDayInfoResult()
    object ServiceDatesNotSet : DDayInfoResult()
} 