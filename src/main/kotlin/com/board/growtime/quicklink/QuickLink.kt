package com.board.growtime.quicklink

import com.board.growtime.common.BaseEntity
import com.board.growtime.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "quick_links")
class QuickLink(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var url: String,

    @Column(name = "favicon_url")
    var faviconUrl: String? = null
) : BaseEntity()