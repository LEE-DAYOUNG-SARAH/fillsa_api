package store.fillsa.fillsa_api.domain.appVersion.domain

import jakarta.persistence.*
import store.fillsa.fillsa_api.common.entity.BaseEntity

@Entity
@Table(name = "app_versions")
class AppVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val appVersionSeq: Long = 0L,

    @Column(nullable = false)
    var minVersion: String,

    @Column(nullable = false)
    var nowVersion: String
): BaseEntity() {
    fun modify(minVersion: String?, nowVersion: String?) {
        minVersion?.takeIf { it.isNotBlank() }?.let { this.minVersion = it }
        nowVersion?.takeIf { it.isNotBlank() }?.let { this.nowVersion = it }
    }
}