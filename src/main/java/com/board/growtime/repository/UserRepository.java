package com.board.growtime.repository;

import com.board.growtime.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // GitHub ID로 사용자 찾기
    Optional<User> findByGithubId(String githubId);
    
    // GitHub 로그인으로 사용자 찾기
    Optional<User> findByLogin(String login);
    
    // GitHub ID로 사용자 존재 여부 확인
    boolean existsByGithubId(String githubId);
    
    // GitHub 로그인으로 사용자 존재 여부 확인
    boolean existsByLogin(String login);
} 