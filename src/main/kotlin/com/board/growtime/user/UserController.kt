package com.board.growtime.user

import com.board.growtime.common.response.ApiResponse
import com.board.growtime.user.dto.DDayInfo
import com.board.growtime.user.dto.GitHubUserRequest
import com.board.growtime.user.dto.ServiceDatesRequest
import com.board.growtime.user.dto.UserInfo
import com.board.growtime.user.dto.toUserInfo
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

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
    fun getUserInfo(@PathVariable githubId: String): ApiResponse<UserInfo> {
        val userInfo = userService.getUserInfo(githubId)
        return ApiResponse.success(userInfo)
    }

    // 입영/제대 날짜 설정
    @PostMapping("/{githubId}/service-dates")
    fun setServiceDates(
        @PathVariable githubId: String,
        @RequestBody request: ServiceDatesRequest
    ): ApiResponse<UserInfo> {
        val user = militaryService.setServiceDates(githubId, request.entryDate, request.dischargeDate)
        // Note: UserService.getUserInfo logic is duplicated here slightly via conversion, 
        // but militaryService returns User entity. We should convert it to UserInfo.
        // Better to use UserMapper here or have MilitaryService return UserInfo.
        // For now, let's use the extension function if accessible or map it.
        // Actually, MilitaryService returns User entity. I should map it to UserInfo.
        // Importing toUserInfo extension function.
        return ApiResponse.success(user.toUserInfo())
    }

    // D-day 정보 조회
    @GetMapping("/{githubId}/d-day")
    fun getDDayInfo(@PathVariable githubId: String): ApiResponse<DDayInfo> {
        val dDayInfo = militaryService.getDDayInfo(githubId)
        return ApiResponse.success(dDayInfo)
    }

    // GitHub 사용자 생성/업데이트
    @PostMapping("/github")
    fun createOrUpdateGitHubUser(
        @RequestBody request: GitHubUserRequest,
        @RequestHeader("Authorization") authorization: String
    ): ApiResponse<UserInfo> {
        val accessToken = authorization.removePrefix("Bearer ").trim()
        
        val user = gitHubUserService.saveOrUpdateUser(
            request.githubId,
            request.login,
            request.name,
            request.avatarUrl,
            request.htmlUrl,
            request.location,
            accessToken
        )
        
        return ApiResponse.success(user.toUserInfo())
    }
} 