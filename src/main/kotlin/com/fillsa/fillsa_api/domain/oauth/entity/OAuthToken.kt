package com.fillsa.fillsa_api.domain.oauth.entity

import com.fillsa.fillsa_api.common.entity.BaseEntity
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "oauth_tokens")
class OAuthToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val oauthTokenSeq: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_SEQ", nullable = false)
    val member: Member,

    @Column(nullable = false)
    val accessToken: String,

    @Column(nullable = false)
    val accessTokenExpiresAt: LocalDateTime,

    @Column(nullable = false)
    val refreshToken: String,

    @Column(nullable = false)
    val refreshTokenExpiresAt: LocalDateTime
): BaseEntity()