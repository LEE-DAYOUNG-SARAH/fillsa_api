package com.fillsa.admin.service.popup

import com.fillsa.service.popup.Popup
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PopupAdminQueryRepository : JpaRepository<Popup, Long> {

    /**
     * 어드민 팝업 목록. popupType·isActive 는 정확일치, keyword 는 제목 부분일치.
     * 세 조건 모두 null 이면 무시된다.
     */
    @Query(
        """
        select p
        from Popup p
        where (:popupType is null or p.popupType = :popupType)
            and (:isActive is null or p.isActive = :isActive)
            and (:keyword is null or lower(p.title) like lower(concat('%', :keyword, '%')))
        """,
    )
    fun search(
        @Param("popupType") popupType: Popup.PopupType?,
        @Param("isActive") isActive: Boolean?,
        @Param("keyword") keyword: String?,
        pageable: Pageable,
    ): Page<Popup>
}
