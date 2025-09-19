package com.board.growtime.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    // GitHub ID로 사용자 찾기
    fun findByGithubId(githubId: String): Optional<User>
    
    // GitHub 로그인으로 사용자 찾기
    fun findByLogin(login: String): Optional<User>
    
    // GitHub ID로 사용자 존재 여부 확인
    fun existsByGithubId(githubId: String): Boolean
} 