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

    /**
     * GitHub ID로 사용자 찾기
     */
    @Transactional(readOnly = true)
    fun findByGithubId(githubId: String): Optional<User> {
        return userRepository.findByGithubId(githubId)
    }

    /**
     * GitHub 로그인으로 사용자 찾기
     */
    @Transactional(readOnly = true)
    fun findByLogin(login: String): Optional<User> {
        return userRepository.findByLogin(login)
    }

    /**
     * 사용자 ID로 사용자 찾기
     */
    @Transactional(readOnly = true)
    fun findById(id: Long): Optional<User> {
        return userRepository.findById(id)
    }

    /**
     * 사용자 존재 여부 확인
     */
    @Transactional(readOnly = true)
    fun existsByGithubId(githubId: String): Boolean {
        return userRepository.existsByGithubId(githubId)
    }

    /**
     * 사용자 삭제
     */
    fun deleteUser(id: Long) {
        userRepository.deleteById(id)
        log.info("사용자 삭제: ID $id")
    }

    /**
     * 사용자 저장 (일반적인 업데이트용)
     */
    fun saveUser(user: User): User {
        return userRepository.save(user)
    }

    /**
     * 모든 사용자 조회
     */
    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    /**
     * 사용자 수 조회
     */
    @Transactional(readOnly = true)
    fun getUserCount(): Long {
        return userRepository.count()
    }
} 