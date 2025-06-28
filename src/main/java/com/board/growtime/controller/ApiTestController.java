package com.board.growtime.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class ApiTestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "안녕하세요! API 테스트 성공입니다.";
    }
    
    @GetMapping("/status")
    public String status() {
        return "서버가 정상적으로 작동 중입니다.";
    }
} 