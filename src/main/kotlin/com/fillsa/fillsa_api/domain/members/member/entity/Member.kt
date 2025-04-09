package com.fillsa.fillsa_api.domain.members.member.entity

import com.fillsa.fillsa_api.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "members")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val memberSeq: Long = 0L,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val oauthProvider: OauthProvider,

    @Column(nullable = false)
    val oauthId: String,

    @Column(nullable = true)
    val email: String? = null,

    @Column(nullable = true)
    val nickname: String? = null,

    @Column(nullable = true)
    val profileImageUrl: String? = null,

    @Column(nullable = true)
    val gender: String? = null,

    @Column(nullable = true)
    val birth: String? = null,

    @Column(nullable = true)
    val locale: String,

    @Column(nullable = false, columnDefinition = "char(1)")
    var withdrawalYn: String = "N",

    @Column(nullable = true)
    var withdrawalAt: LocalDateTime? = null
): BaseEntity() {
    enum class OauthProvider {
        KAKAO, GOOGLE
    }
}