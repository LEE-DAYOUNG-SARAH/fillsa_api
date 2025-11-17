package store.fillsa.fillsa_api.domain.popup.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import store.fillsa.fillsa_api.domain.popup.entity.Popup
import java.time.LocalDateTime

interface PopupRepository: JpaRepository<Popup, Long> {
    @Query("""
        SELECT p
        FROM Popup p
        WHERE p.isActive = true
            AND p.startDateTime <= :now
            AND p.endDateTime >= :now
            AND p.popupType != 'VERSION_UPDATE'
        ORDER BY
            CASE p.popupType
                WHEN 'NOTICE' THEN 0
                ELSE 1
            END ASC,
            p.createdAt DESC
        LIMIT 1
    """)
    fun findActiveGeneralPopup(now: LocalDateTime): Popup?

    @Query("""
        SELECT p
        FROM Popup p
        WHERE p.isActive = true
            AND p.startDateTime <= :now
            AND p.endDateTime >= :now
            AND p.popupType = 'VERSION_UPDATE'
            AND p.targetVersion = :currentVersion
        ORDER BY p.createdAt DESC
        LIMIT 1
    """)
    fun findLatestActiveVersionUpdatePopup(now: LocalDateTime, currentVersion: String): Popup?
}