package com.board.growtime.service;

import com.board.growtime.entity.User;
import com.board.growtime.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * GitHub 사용자 정보로 사용자를 저장하거나 업데이트
     */
    public User saveOrUpdateUser(String githubId, String login, String name, String email,
                                String avatarUrl, String htmlUrl, String company, 
                                String location, String bio, String accessToken) {
        
        Optional<User> existingUser = userRepository.findByGithubId(githubId);
        
        if (existingUser.isPresent()) {
            // 기존 사용자 정보 업데이트
            User user = existingUser.get();
            user.updateUserInfo(name, email, avatarUrl, htmlUrl, company, location, bio);
            user.updateAccessToken(accessToken);
            
            log.info("기존 사용자 정보 업데이트: {}", login);
            return userRepository.save(user);
        } else {
            // 새 사용자 생성
            User newUser = new User(githubId, login, name, email, avatarUrl, htmlUrl, company, location, bio);
            newUser.updateAccessToken(accessToken);
            
            log.info("새 사용자 생성: {}", login);
            return userRepository.save(newUser);
        }
    }

    /**
     * GitHub ID로 사용자 찾기
     */
    @Transactional(readOnly = true)
    public Optional<User> findByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId);
    }

    /**
     * GitHub 로그인으로 사용자 찾기
     */
    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    /**
     * 사용자 ID로 사용자 찾기
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 사용자 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByGithubId(String githubId) {
        return userRepository.existsByGithubId(githubId);
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.info("사용자 삭제: ID {}", id);
    }
} 