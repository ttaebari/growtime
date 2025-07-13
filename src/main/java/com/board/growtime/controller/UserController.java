package com.board.growtime.controller;

import com.board.growtime.entity.User;
import com.board.growtime.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // 사용자 정보 조회
    @GetMapping("/{githubId}")
    public ResponseEntity<?> getUserInfo(@PathVariable String githubId) {
        Optional<User> userOpt = userService.findByGithubId(githubId);
        
        if (userOpt.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "사용자를 찾을 수 없습니다");
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Map<String, Object> response = createUserResponse(user);
        return ResponseEntity.ok(response);
    }

    // 입영/제대 날짜 설정
    @PostMapping("/{githubId}/service-dates")
    public ResponseEntity<?> setServiceDates(
            @PathVariable String githubId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dischargeDate) {
        
        Optional<User> userOpt = userService.findByGithubId(githubId);
        
        if (userOpt.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "사용자를 찾을 수 없습니다");
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setServiceDates(entryDate, dischargeDate);
        User updatedUser = userService.saveUser(user);

        Map<String, Object> response = createUserResponse(updatedUser);
        response.put("message", "복무 날짜가 성공적으로 설정되었습니다");
        
        log.info("사용자 복무 날짜 설정: {} - 입영: {}, 제대: {}", 
                githubId, entryDate, dischargeDate);
        
        return ResponseEntity.ok(response);
    }

    // D-day 정보 조회
    @GetMapping("/{githubId}/d-day")
    public ResponseEntity<?> getDDayInfo(@PathVariable String githubId) {
        Optional<User> userOpt = userService.findByGithubId(githubId);
        
        if (userOpt.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "사용자를 찾을 수 없습니다");
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        
        if (user.getEntryDate() == null || user.getDischargeDate() == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "복무 날짜가 설정되지 않았습니다");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> dDayInfo = new HashMap<>();
        dDayInfo.put("dDay", user.calculateDDay());
        dDayInfo.put("serviceDays", user.calculateServiceDays());
        dDayInfo.put("totalServiceDays", user.calculateTotalServiceDays());
        dDayInfo.put("entryDate", user.getEntryDate());
        dDayInfo.put("dischargeDate", user.getDischargeDate());
        dDayInfo.put("progressPercentage", calculateProgressPercentage(user));

        return ResponseEntity.ok(dDayInfo);
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("githubId", user.getGithubId());
        userResponse.put("login", user.getLogin());
        userResponse.put("name", user.getName());
        userResponse.put("avatarUrl", user.getAvatarUrl());
        userResponse.put("htmlUrl", user.getHtmlUrl());
        userResponse.put("location", user.getLocation());
        userResponse.put("entryDate", user.getEntryDate());
        userResponse.put("dischargeDate", user.getDischargeDate());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        return userResponse;
    }

    private double calculateProgressPercentage(User user) {
        if (user.getEntryDate() == null || user.getDischargeDate() == null) {
            return 0.0;
        }
        
        long totalDays = user.calculateTotalServiceDays();
        long serviceDays = user.calculateServiceDays();
        
        if (totalDays <= 0) {
            return 0.0;
        }
        
        double percentage = (double) serviceDays / totalDays * 100;
        return Math.min(Math.max(percentage, 0.0), 100.0); // 0-100 범위로 제한
    }
} 