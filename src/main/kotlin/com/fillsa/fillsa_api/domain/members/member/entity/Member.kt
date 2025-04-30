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

    @Column(nullable = false, columnDefinition = "char(1)")
    var withdrawalYn: String = "N",

    @Column(nullable = true)
    var withdrawalAt: LocalDateTime? = null
): BaseEntity(), UserDetails {

    fun withdrawal() {
        this.withdrawalYn = "Y"
        this.withdrawalAt = LocalDateTime.now()
    }

    fun isWithdrawal() = this.withdrawalYn == "Y"

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String? = null

    override fun getUsername(): String = memberSeq.toString()

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    enum class OAuthProvider {
        KAKAO, GOOGLE;

        companion object {
            fun fromPath(path: String): OAuthProvider =
                entries.find { it.name.equals(path, ignoreCase = true) }
                    ?: throw IllegalArgumentException("지원하지 않는 OAuth Provider: $path")
        }
    }
}