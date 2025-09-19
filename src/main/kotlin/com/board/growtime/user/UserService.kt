package com.board.growtime.user

import com.board.growtime.user.dto.UserInfo
import com.board.growtime.user.result.UserInfoResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 사용자 기본 정보 관리를 담당하는 서비스
 * 순수하게 사용자 CRUD 기능만 제공
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    /**
     * 사용자 정보 조회 및 응답 생성
     */
    @Transactional(readOnly = true)
    fun getUserInfo(githubId: String): UserInfoResult {
        val userOpt = userRepository.findByGithubId(githubId)
        
        if (userOpt.isEmpty) {
            return UserInfoResult.UserNotFound
        }

        val user = userOpt.get()
        val userInfo = UserInfo(
            id = user.id,
            githubId = user.githubId,
            login = user.login,
            name = user.name,
            avatarUrl = user.avatarUrl,
            htmlUrl = user.htmlUrl,
            location = user.location,
            entryDate = user.entryDate,
            dischargeDate = user.dischargeDate,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )

        return UserInfoResult.Success(userInfo)
    }
} 