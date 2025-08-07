package com.board.growtime.user

import com.board.growtime.user.dto.DDayInfo
import com.board.growtime.user.result.DDayInfoResult
import com.board.growtime.user.result.ServiceDateResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

/**
 * 군 복무 관련 비즈니스 로직을 담당하는 서비스
 */
@Service
@Transactional
class MilitaryService(
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(MilitaryService::class.java)

    /**
     * 복무 날짜 설정 비즈니스 로직
     */
    fun setServiceDates(githubId: String, entryDate: LocalDate, dischargeDate: LocalDate): ServiceDateResult {
        val userOpt = userRepository.findByGithubId(githubId)
        
        if (userOpt.isEmpty) {
            return ServiceDateResult.UserNotFound
        }

        // 날짜 유효성 검증
        if (entryDate.isAfter(dischargeDate)) {
            return ServiceDateResult.InvalidDates("입영일이 제대일보다 늦을 수 없습니다")
        }

        // 미래 날짜 검증
        val today = LocalDate.now()
        if (entryDate.isAfter(today.plusYears(1))) {
            return ServiceDateResult.InvalidDates("입영일이 너무 먼 미래입니다")
        }

        val user = userOpt.get()
        user.setServiceDates(entryDate, dischargeDate)
        val updatedUser = userRepository.save(user)

        log.info("사용자 복무 날짜 설정: {} - 입영: {}, 제대: {}", githubId, entryDate, dischargeDate)
        
        return ServiceDateResult.Success(updatedUser)
    }

    /**
     * D-day 정보 조회 비즈니스 로직
     */
    @Transactional(readOnly = true)
    fun getDDayInfo(githubId: String): DDayInfoResult {
        val userOpt = userRepository.findByGithubId(githubId)
        
        if (userOpt.isEmpty) {
            return DDayInfoResult.UserNotFound
        }

        val user = userOpt.get()
        
        if (user.entryDate == null || user.dischargeDate == null) {
            return DDayInfoResult.ServiceDatesNotSet
        }

        val dDayInfo = DDayInfo(
            dDay = user.calculateDDay(),
            serviceDays = user.calculateServiceDays(),
            totalServiceDays = user.calculateTotalServiceDays(),
            entryDate = user.entryDate!!,
            dischargeDate = user.dischargeDate!!,
            progressPercentage = calculateProgressPercentage(user)
        )

        return DDayInfoResult.Success(dDayInfo)
    }

    /**
     * 복무 진행률 계산 (비즈니스 로직)
     */
    private fun calculateProgressPercentage(user: User): Double {
        if (user.entryDate == null || user.dischargeDate == null) {
            return 0.0
        }
        
        val totalDays = user.calculateTotalServiceDays()
        val serviceDays = user.calculateServiceDays()
        
        if (totalDays <= 0) {
            return 0.0
        }
        
        val percentage = serviceDays.toDouble() / totalDays * 100
        return min(max(percentage, 0.0), 100.0) // 0-100 범위로 제한
    }

    /**
     * 복무 상태 확인 (전역 여부 등)
     */
    @Transactional(readOnly = true)
    fun getServiceStatus(githubId: String): ServiceStatus {
        val userOpt = userRepository.findByGithubId(githubId)
        
        if (userOpt.isEmpty) {
            return ServiceStatus.UserNotFound
        }

        val user = userOpt.get()
        
        if (user.entryDate == null || user.dischargeDate == null) {
            return ServiceStatus.NotSet
        }

        val today = LocalDate.now()
        return when {
            today.isBefore(user.entryDate) -> ServiceStatus.BeforeEntry
            today.isAfter(user.dischargeDate) -> ServiceStatus.Discharged
            else -> ServiceStatus.Serving
        }
    }
}

/**
 * 복무 상태를 나타내는 enum
 */
enum class ServiceStatus {
    UserNotFound,
    NotSet,
    BeforeEntry,
    Serving,
    Discharged
} 