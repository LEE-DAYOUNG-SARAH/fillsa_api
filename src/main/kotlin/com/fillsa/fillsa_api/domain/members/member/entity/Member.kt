package com.fillsa.fillsa_api.domain.members.member.entity

import com.fillsa.fillsa_api.common.entity.BaseEntity
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "members")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val memberSeq: Long = 0L,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val oauthProvider: OAuthProvider,

    @Column(nullable = false)
    val oauthId: String,

    @Column(nullable = true)
    val nickname: String? = null,

    @Column(nullable = true)
    val profileImageUrl: String? = null,

    @Column(nullable = true)
    val gender: String? = null,

    @Column(nullable = true)
    val birth: String? = null,

    @Column(nullable = true)
    val locale: String? = null,

    @Column(nullable = false, columnDefinition = "char(1)")
    var withdrawalYn: String = "N",

    @Column(nullable = true)
    var withdrawalAt: LocalDateTime? = null
): BaseEntity(), UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String? = null

    override fun getUsername(): String = memberSeq.toString()

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    companion object {
        @JvmStatic
        fun createOAuthMember(
            oauthId: String,
            oauthProvider: OAuthProvider,
            nickname: String,
            profileImageUrl: String? = null
        ): Member {
            return Member(
                oauthId = oauthId,
                oauthProvider = oauthProvider,
                nickname = nickname,
                profileImageUrl = profileImageUrl
            )
        }
    }
    enum class OAuthProvider {
        KAKAO, GOOGLE
    }
}