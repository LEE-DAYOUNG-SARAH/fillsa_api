package com.fillsa.admin.service.popup

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.service.popup.Popup
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PopupAdminService(
    private val popupAdminRepository: PopupAdminQueryRepository,
) {

    @Transactional(readOnly = true)
    fun list(
        pageable: Pageable,
        popupType: Popup.PopupType?,
        isActive: Boolean?,
        keyword: String?,
    ): PageEnvelope<PopupResponse> {
        val effectivePageable = withDefaultSort(pageable)
        val page = popupAdminRepository.search(
            popupType = popupType,
            isActive = isActive,
            keyword = keyword?.takeIf { it.isNotBlank() },
            pageable = effectivePageable,
        )

        return PageEnvelope.from(page) { popup -> PopupResponse.from(popup) }
    }

    @Transactional
    fun create(request: PopupSaveRequest): PopupResponse {
        val popup = Popup(
            popupType = request.popupType,
            title = request.title,
            content = request.content,
            imageUrl = request.imageUrl,
            startDateTime = request.startDateTime,
            endDateTime = request.endDateTime,
            isActive = request.isActive,
            targetVersion = request.targetVersion,
        )
        val saved = popupAdminRepository.save(popup)
        return PopupResponse.from(saved)
    }

    @Transactional
    fun update(popupSeq: Long, request: PopupSaveRequest): PopupResponse {
        val popup = getPopup(popupSeq)
        popup.popupType = request.popupType
        popup.title = request.title
        popup.content = request.content
        popup.imageUrl = request.imageUrl
        popup.startDateTime = request.startDateTime
        popup.endDateTime = request.endDateTime
        popup.isActive = request.isActive
        popup.targetVersion = request.targetVersion

        return PopupResponse.from(popup)
    }

    @Transactional
    fun delete(popupSeq: Long) {
        val popup = getPopup(popupSeq)
        popupAdminRepository.delete(popup)
    }

    @Transactional
    fun toggleActive(popupSeq: Long, isActive: Boolean): PopupResponse {
        val popup = getPopup(popupSeq)
        popup.isActive = isActive
        return PopupResponse.from(popup)
    }

    private fun getPopup(popupSeq: Long): Popup =
        popupAdminRepository.findById(popupSeq)
            .orElseThrow { BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 popupSeq: $popupSeq") }

    private fun withDefaultSort(pageable: Pageable): Pageable =
        if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "popupSeq"))
        }
}
