package com.board.growtime.quicklink

import org.springframework.data.jpa.repository.JpaRepository

interface QuickLinkRepository : JpaRepository<QuickLink, Long> {
    fun findAllByUser_GithubIdOrderByCreatedAtAsc(githubId: String): List<QuickLink>
}