package com.board.growtime.user

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)

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
        
        return if (existingUser.isPresent) {
            // 기존 사용자 정보 업데이트
            val user = existingUser.get()
            user.updateUserInfo(name, avatarUrl, htmlUrl, location)
            user.updateAccessToken(accessToken)
            
            log.info("기존 사용자 정보 업데이트: $login")
            userRepository.save(user)
        } else {
            // 새 사용자 생성
            val newUser = User(githubId, login, name, avatarUrl, htmlUrl, location, accessToken)
            
            log.info("새 사용자 생성: $login")
            userRepository.save(newUser)
        }
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
     * 사용자 저장
     */
    fun saveUser(user: User): User {
        return userRepository.save(user)
    }
} 