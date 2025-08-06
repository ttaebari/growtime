package com.board.growtime.user

import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(UserController::class.java)

    // 사용자 정보 조회
    @GetMapping("/{githubId}")
    fun getUserInfo(@PathVariable githubId: String): ResponseEntity<*> {
        val userOpt = userService.findByGithubId(githubId)
        
        if (userOpt.isEmpty) {
            return ResponseEntity.notFound().build<Any>()
        }

        val user = userOpt.get()
        val response = createUserResponse(user)
        return ResponseEntity.ok(response)
    }

    // 입영/제대 날짜 설정
    @PostMapping("/{githubId}/service-dates")
    fun setServiceDates(
        @PathVariable githubId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) entryDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dischargeDate: LocalDate
    ): ResponseEntity<*> {
        val userOpt = userService.findByGithubId(githubId)
        
        if (userOpt.isEmpty) {
            return ResponseEntity.notFound().build<Any>()
        }

        val user = userOpt.get()
        user.setServiceDates(entryDate, dischargeDate)
        val updatedUser = userService.saveUser(user)

        val response = createUserResponse(updatedUser).toMutableMap()
        response["message"] = "복무 날짜가 성공적으로 설정되었습니다"
        
        log.info("사용자 복무 날짜 설정: {} - 입영: {}, 제대: {}", githubId, entryDate, dischargeDate)
        
        return ResponseEntity.ok(response)
    }

    // D-day 정보 조회
    @GetMapping("/{githubId}/d-day")
    fun getDDayInfo(@PathVariable githubId: String): ResponseEntity<*> {
        val userOpt = userService.findByGithubId(githubId)
        
        if (userOpt.isEmpty) {
            return ResponseEntity.notFound().build<Any>()
        }

        val user = userOpt.get()
        
        if (user.entryDate == null || user.dischargeDate == null) {
            val errorResponse = mapOf("error" to "복무 날짜가 설정되지 않았습니다")
            return ResponseEntity.badRequest().body(errorResponse)
        }

        val dDayInfo = mapOf(
            "dDay" to user.calculateDDay(),
            "serviceDays" to user.calculateServiceDays(),
            "totalServiceDays" to user.calculateTotalServiceDays(),
            "entryDate" to user.entryDate,
            "dischargeDate" to user.dischargeDate,
            "progressPercentage" to calculateProgressPercentage(user)
        )

        return ResponseEntity.ok(dDayInfo)
    }

    private fun createUserResponse(user: User): Map<String, Any?> {
        return mapOf(
            "id" to user.id,
            "githubId" to user.githubId,
            "login" to user.login,
            "name" to user.name,
            "avatarUrl" to user.avatarUrl,
            "htmlUrl" to user.htmlUrl,
            "location" to user.location,
            "entryDate" to user.entryDate,
            "dischargeDate" to user.dischargeDate,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt
        )
    }

    private fun calculateProgressPercentage(user: User): Double {
        if (user.entryDate == null || user.dischargeDate == null) {
            return 0.0
        }
        
        val totalDays = user.calculateTotalServiceDays()
        val serviceDays = user.calculateServiceDays()
        
        if (totalDays <= 0) {
            return 0.0
        }
        
        val percentage = serviceDays.toDouble() / totalDays * 100
        return min(max(percentage, 0.0), 100.0) // 0-100 범위로 제한
    }
} 