package com.board.growtime.user

import com.board.growtime.core.exception.InvalidServiceDateException
import com.board.growtime.core.exception.ServiceDateNotSetException
import com.board.growtime.core.exception.UserNotFoundException
import com.board.growtime.core.util.DateUtils
import com.board.growtime.core.util.ValidationUtils
import com.board.growtime.enums.ServiceStatus
import com.board.growtime.user.dto.DDayInfo
import com.board.growtime.user.result.DDayInfoResult
import com.board.growtime.user.result.ServiceDateResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 군 복무 관련 비즈니스 로직을 담당하는 서비스
 * 
 * 학습 포인트:
 * 1. Service Layer 패턴 - 복무 관련 비즈니스 로직 캡슐화
 * 2. 도메인 지식 집중 - 복무 관련 계산 로직을 한 곳에서 관리
 * 3. 유틸리티 클래스 활용 - 공통 로직을 재사용 가능한 형태로 분리
 * 4. 예외 처리 통일 - 명확한 예외 타입으로 에러 상황 구분
 */
@Service
@Transactional
class MilitaryService(
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(MilitaryService::class.java)

    /**
     * 복무 날짜 설정 (비즈니스 로직 포함)
     * 
     * 학습 포인트:
     * - 입력값 검증을 서비스 레이어에서 담당
     * - 비즈니스 규칙을 명확히 정의하고 적용
     * - 로깅을 통한 중요한 비즈니스 이벤트 추적
     */
    fun setServiceDates(githubId: String, entryDate: LocalDate, dischargeDate: LocalDate): ServiceDateResult {
        // 비즈니스 로직: 입력값 검증
        validateGitHubId(githubId)
        validateServiceDates(entryDate, dischargeDate)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
        if (userOpt.isEmpty) {
            log.warn("복무 날짜 설정 실패 - 사용자 없음: {}", githubId)
            return ServiceDateResult.UserNotFound
        }

        val user = userOpt.get()
        user.setServiceDates(entryDate, dischargeDate)
        val updatedUser = userRepository.save(user)

        log.info("복무 날짜 설정 완료: {} - 입영: {}, 제대: {}", githubId, entryDate, dischargeDate)
        
        return ServiceDateResult.Success(updatedUser)
    }

    /**
     * D-day 정보 조회 (비즈니스 로직 포함)
     */
    @Transactional(readOnly = true)
    fun getDDayInfo(githubId: String): DDayInfoResult {
        validateGitHubId(githubId)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
        if (userOpt.isEmpty) {
            return DDayInfoResult.UserNotFound
        }

        val user = userOpt.get()
        
        if (user.entryDate == null || user.dischargeDate == null) {
            return DDayInfoResult.ServiceDatesNotSet
        }

        val dDayInfo = calculateDDayInfo(user)
        return DDayInfoResult.Success(dDayInfo)
    }

    /**
     * 복무 상태 확인 (비즈니스 로직)
     */
    @Transactional(readOnly = true)
    fun getServiceStatus(githubId: String): ServiceStatus {
        validateGitHubId(githubId)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
        if (userOpt.isEmpty) {
            return ServiceStatus.UserNotFound
        }

        val user = userOpt.get()
        
        if (user.entryDate == null || user.dischargeDate == null) {
            return ServiceStatus.NotSet
        }

        return determineServiceStatus(user.entryDate!!, user.dischargeDate!!)
    }

    /**
     * 복무 진행률 조회
     */
    @Transactional(readOnly = true)
    fun getServiceProgress(githubId: String): Double {
        validateGitHubId(githubId)
        
        val userOpt = userRepository.findByGithubId(githubId.trim())
        if (userOpt.isEmpty) {
            throw UserNotFoundException(githubId)
        }

        val user = userOpt.get()
        
        if (user.entryDate == null || user.dischargeDate == null) {
            throw ServiceDateNotSetException()
        }

        return DateUtils.calculateServiceProgress(user.entryDate!!, user.dischargeDate!!)
    }

    /**
     * GitHub ID 검증
     */
    private fun validateGitHubId(githubId: String) {
        if (!ValidationUtils.isValidGitHubId(githubId)) {
            throw InvalidServiceDateException("올바른 GitHub ID 형식이 아닙니다: $githubId")
        }
    }

    /**
     * 복무 날짜 검증 (비즈니스 규칙)
     */
    private fun validateServiceDates(entryDate: LocalDate, dischargeDate: LocalDate) {
        val today = LocalDate.now()
        
        // 비즈니스 규칙: 날짜 유효성 검증
        if (entryDate >= dischargeDate) {
            throw InvalidServiceDateException("입영일은 제대일보다 이전이어야 합니다")
        }

        if (entryDate > today) {
            throw InvalidServiceDateException("입영일은 오늘보다 이전이어야 합니다")
        }

        // 비즈니스 규칙: 복무 기간 검증
        if (!DateUtils.isValidServicePeriod(entryDate, dischargeDate)) {
            throw InvalidServiceDateException("복무 기간이 비정상적입니다. (1년 ~ 4년 사이)")
        }
    }

    /**
     * D-day 정보 계산 (비즈니스 로직)
     */
    private fun calculateDDayInfo(user: User): DDayInfo {
        val entryDate = user.entryDate!!
        val dischargeDate = user.dischargeDate!!
        
        return DDayInfo(
            dDayCount = user.calculateDDay(),
            serviceDays = user.calculateServiceDays(),
            totalServiceDays = user.calculateTotalServiceDays(),
            entryDate = entryDate,
            dischargeDate = dischargeDate,
            progressPercentage = DateUtils.calculateServiceProgress(entryDate, dischargeDate)
        )
    }

    /**
     * 복무 상태 결정 (비즈니스 로직)
     */
    private fun determineServiceStatus(entryDate: LocalDate, dischargeDate: LocalDate): ServiceStatus {
        val today = LocalDate.now()
        
        return when {
            today.isBefore(entryDate) -> ServiceStatus.BeforeEntry
            today.isAfter(dischargeDate) -> ServiceStatus.Discharged
            else -> ServiceStatus.Serving
        }
    }
}