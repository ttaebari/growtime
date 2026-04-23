package com.board.growtime.user

import com.board.growtime.common.exception.UserNotFoundException
import com.board.growtime.common.exception.InvalidUserDataException
import com.board.growtime.common.util.ValidationUtils
import com.board.growtime.user.dto.UserInfo
import com.board.growtime.user.dto.toUserInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 기본 정보 관리를 담당하는 서비스
 * 
 * 학습 포인트:
 * 1. Service Layer 패턴 - 비즈니스 로직을 컨트롤러에서 분리
 * 2. 일관된 서비스 구조 - 모든 서비스가 동일한 패턴을 따름
 * 3. 예외 처리 통일 - 커스텀 예외를 사용한 명확한 에러 처리
 * 4. 검증 로직 분리 - 유틸리티 클래스를 활용한 입력값 검증
 */
@Service
class UserService(
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    fun getUserInfo(githubId: String): UserInfo {
        validateGitHubId(githubId)
        
        val user = userRepository.findByGithubId(githubId.trim())
            ?: run {
                log.warn("사용자 조회 실패: {}", githubId)
                throw UserNotFoundException(githubId)
            }
        
        log.debug("사용자 조회 성공: {}", githubId)
        return user.toUserInfo()
    }

    /**
     * GitHub ID 검증
     */
    private fun validateGitHubId(githubId: String) {
        if (!ValidationUtils.isValidGitHubId(githubId)) {
            throw InvalidUserDataException("올바른 GitHub ID 형식이 아닙니다: $githubId")
        }
    }
}
