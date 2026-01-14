package com.board.growtime.user.dto

import com.board.growtime.user.User

fun User.toUserInfo(): UserInfo {
    return UserInfo(
        id = this.id,
        githubId = this.githubId,
        login = this.login,
        name = this.name,
        avatarUrl = this.avatarUrl,
        htmlUrl = this.htmlUrl,
        location = this.location,
        entryDate = this.entryDate,
        dischargeDate = this.dischargeDate,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
