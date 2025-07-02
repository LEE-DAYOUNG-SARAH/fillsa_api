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
    val minVersion: String
): BaseEntity()