package store.fillsa.fillsa_api.domain.members.member.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import store.fillsa.fillsa_api.common.entity.BaseEntity
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.INVALID_VALUE
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
    var nickname: String? = null,

    @Column(nullable = true)
    var profileImageUrl: String? = null,

    @Column(nullable = false, columnDefinition = "char(1)")
    var withdrawalYn: String = "N",

    @Column(nullable = true)
    var withdrawalAt: LocalDateTime? = null,

    @Column(nullable = false, columnDefinition = "char(1)")
    var adminYn: String = "N",
): BaseEntity(), UserDetails {
    enum class OAuthProvider {
        KAKAO, GOOGLE;

        companion object {
            fun fromPath(path: String): OAuthProvider =
                entries.find { it.name.equals(path, ignoreCase = true) }
                    ?: throw BusinessException(INVALID_VALUE, "지원하지 않는 OAuth Provider: $path")
        }
    }

    fun withdrawal() {
        this.withdrawalYn = "Y"
        this.withdrawalAt = LocalDateTime.now()
    }

    fun isWithdrawal() = this.withdrawalYn == "Y"

    fun update(nickname: String, profileImageUrl: String?) {
        this.nickname = nickname
        this.profileImageUrl = profileImageUrl
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String? = null

    override fun getUsername(): String = memberSeq.toString()

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}