package com.board.growtime.user

import com.board.growtime.user.dto.UserInfo
import com.board.growtime.user.result.DDayInfoResult
import com.board.growtime.user.result.ServiceDateResult
import com.board.growtime.user.result.UserInfoResult
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val gitHubUserService: GitHubUserService,
    private val militaryService: MilitaryService
) {
    private val log = LoggerFactory.getLogger(UserController::class.java)

    // 사용자 정보 조회
    @GetMapping("/{githubId}")
    fun getUserInfo(@PathVariable githubId: String): ResponseEntity<*> {
        return when (val result = userService.getUserInfo(githubId)) {
            is UserInfoResult.Success -> ResponseEntity.ok(result.userInfo)
            is UserInfoResult.UserNotFound -> ResponseEntity.notFound().build<Any>()
        }
    }

    // 입영/제대 날짜 설정
    @PostMapping("/{githubId}/service-dates")
    fun setServiceDates(
        @PathVariable githubId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) entryDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dischargeDate: LocalDate
    ): ResponseEntity<*> {
        return when (val result = militaryService.setServiceDates(githubId, entryDate, dischargeDate)) {
            is ServiceDateResult.Success -> {
                val userInfo = userService.getUserInfo(githubId)
                if (userInfo is UserInfoResult.Success) {
                    val response = userInfo.userInfo.toMap().toMutableMap()
                    response["message"] = "복무 날짜가 성공적으로 설정되었습니다"
                    ResponseEntity.ok(response)
                } else {
                    ResponseEntity.notFound().build<Any>()
                }
            }
            is ServiceDateResult.UserNotFound -> ResponseEntity.notFound().build<Any>()
            is ServiceDateResult.InvalidDates -> {
                val errorResponse = mapOf("error" to result.message)
                ResponseEntity.badRequest().body(errorResponse)
            }
        }
    }

    // D-day 정보 조회
    @GetMapping("/{githubId}/d-day")
    fun getDDayInfo(@PathVariable githubId: String): ResponseEntity<*> {
        return when (val result = militaryService.getDDayInfo(githubId)) {
            is DDayInfoResult.Success -> ResponseEntity.ok(result.dDayInfo)
            is DDayInfoResult.UserNotFound -> ResponseEntity.notFound().build<Any>()
            is DDayInfoResult.ServiceDatesNotSet -> {
                val errorResponse = mapOf("error" to "복무 날짜가 설정되지 않았습니다")
                ResponseEntity.badRequest().body(errorResponse)
            }
        }
    }

    // GitHub 사용자 생성/업데이트 (새로운 엔드포인트)
    @PostMapping("/github")
    fun createOrUpdateGitHubUser(
        @RequestParam githubId: String,
        @RequestParam login: String,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) avatarUrl: String?,
        @RequestParam(required = false) htmlUrl: String?,
        @RequestParam(required = false) location: String?,
        @RequestParam accessToken: String
    ): ResponseEntity<*> {
        val user = gitHubUserService.saveOrUpdateUser(githubId, login, name, avatarUrl, htmlUrl, location, accessToken)
        
        val userInfo = userService.getUserInfo(githubId)
        return if (userInfo is UserInfoResult.Success) {
            ResponseEntity.ok(userInfo.userInfo)
        } else {
            ResponseEntity.notFound().build<Any>()
        }
    }
}

// UserInfo를 Map으로 변환하는 확장 함수
private fun UserInfo.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "githubId" to githubId,
        "login" to login,
        "name" to name,
        "avatarUrl" to avatarUrl,
        "htmlUrl" to htmlUrl,
        "location" to location,
        "entryDate" to entryDate,
        "dischargeDate" to dischargeDate,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
} 