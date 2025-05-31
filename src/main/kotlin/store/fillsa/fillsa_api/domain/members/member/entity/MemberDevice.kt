package store.fillsa.fillsa_api.domain.members.member.entity

import jakarta.persistence.*
import store.fillsa.fillsa_api.common.entity.BaseEntity

@Entity
@Table(name = "member_devices")
class MemberDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val memberDeviceSeq: Long = 0L,

    @Column(nullable = false)
    val deviceId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_SEQ", nullable = false)
    val member: Member,

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    val osType: OsType = OsType.ANDROID,

    @Column(nullable = true)
    val deviceModel: String,

    @Column(nullable = true)
    var appVersion: String,

    @Column(nullable = true)
    var osVersion: String,

    @Column(nullable = false, columnDefinition = "char(1)")
    var activeYn: String = "Y"
): BaseEntity() {
    enum class OsType {
        ANDROID, IOS;
    }

    fun update(appVersion: String, osVersion: String) {
        this.appVersion = appVersion
        this.osVersion = osVersion
        updateActiveYn("Y")
    }

    fun logout() {
        updateActiveYn("N")
    }

    private fun updateActiveYn(activeYn: String) {
        this.activeYn = activeYn
    }
}