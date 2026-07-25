package com.fillsa.service.member

import jakarta.persistence.*
import com.fillsa.util.entity.BaseEntity
import java.time.LocalDateTime

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
    var activeYn: String = "Y",

    /** FCM 등록 토큰 (iOS 서버 발송용). null = 미등록/무효화됨 */
    @Column(nullable = true, length = 512)
    var pushToken: String? = null,

    /** 푸시 수신 동의 여부 — 'Y' 인 디바이스에만 발송한다 */
    @Column(nullable = false, columnDefinition = "char(1)")
    var pushAgreedYn: String = "N",

    @Column(nullable = true)
    var pushAgreedAt: LocalDateTime? = null,
): BaseEntity() {
    enum class OsType {
        ANDROID, IOS;
    }

    fun update(appVersion: String, osVersion: String) {
        this.appVersion = appVersion
        this.osVersion = osVersion
        updateActiveYn("Y")
    }

    /** 푸시 토큰·동의 갱신 (권한 허용/거부, 설정 토글, 토큰 리프레시 시) */
    fun updatePush(pushToken: String?, agreed: Boolean) {
        this.pushToken = pushToken
        if (agreed && this.pushAgreedYn != "Y") {
            this.pushAgreedAt = LocalDateTime.now()
        }
        this.pushAgreedYn = if (agreed) "Y" else "N"
    }

    /** 발송 실패(UNREGISTERED 등)로 무효화된 토큰 정리 */
    fun clearPushToken() {
        this.pushToken = null
    }

    fun logout() {
        updateActiveYn("N")
    }

    private fun updateActiveYn(activeYn: String) {
        this.activeYn = activeYn
    }
}