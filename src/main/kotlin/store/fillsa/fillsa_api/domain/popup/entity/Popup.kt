package store.fillsa.fillsa_api.domain.popup.entity

import jakarta.persistence.*
import store.fillsa.fillsa_api.common.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "popups")
class Popup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val popupSeq: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var popupType: PopupType,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false, length = 500)
    var imageUrl: String,

    @Column(nullable = false)
    var startDateTime: LocalDateTime,

    @Column(nullable = false)
    var endDateTime: LocalDateTime,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(length = 20)
    var targetVersion: String? = null
) : BaseEntity() {
    enum class PopupType() {
        NOTICE,
        VERSION_UPDATE,
        EVENT
    }
}