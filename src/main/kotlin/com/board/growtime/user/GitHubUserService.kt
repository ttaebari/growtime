package com.board.growtime.user

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * GitHub 사용자 연동 관련 비즈니스 로직을 담당하는 서비스
 */
@Service
@Transactional
class GitHubUserService(
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(GitHubUserService::class.java)

    /**
     * GitHub 사용자 정보로 사용자를 저장하거나 업데이트
     */
    fun saveOrUpdateUser(
        githubId: String,
        login: String,
        name: String?,
        avatarUrl: String?,
        htmlUrl: String?,
        location: String?,
        accessToken: String
    ): User {
        val existingUser = userRepository.findByGithubId(githubId)
        
        return if (existingUser != null) {
            updateExistingUser(existingUser, name, avatarUrl, htmlUrl, location, accessToken, login)
        } else {
            createNewUser(githubId, login, name, avatarUrl, htmlUrl, location, accessToken)
        }
    }

    /**
     * 기존 사용자 정보 업데이트
     */
    private fun updateExistingUser(
        user: User,
        name: String?,
        avatarUrl: String?,
        htmlUrl: String?,
        location: String?,
        accessToken: String,
        login: String
    ): User {
        user.updateUserInfo(name, avatarUrl, htmlUrl, location)
        user.updateAccessToken(accessToken)
        
        log.info("기존 사용자 정보 업데이트: $login")
        return userRepository.save(user)
    }

    /**
     * 새 사용자 생성
     */
    private fun createNewUser(
        githubId: String,
        login: String,
        name: String?,
        avatarUrl: String?,
        htmlUrl: String?,
        location: String?,
        accessToken: String
    ): User {
        val newUser = User(githubId, login, name, avatarUrl, htmlUrl, location, accessToken)
        
        log.info("새 사용자 생성: $login")
        return userRepository.save(newUser)
    }
} 