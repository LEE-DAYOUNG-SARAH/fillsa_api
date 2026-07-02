package com.fillsa.admin.common.dto

import org.springframework.data.domain.Page

/**
 * 계약(admin-api.yaml)의 페이지 응답 구조: { content, page: { number, size, totalElements, totalPages } }.
 * Spring Boot PagedModel 직렬화 형식과 동일한 필드를 명시적으로 구성한다.
 */
data class PageInfo(
    val number: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class PageEnvelope<T>(
    val content: List<T>,
    val page: PageInfo,
) {
    companion object {
        fun <S, T> from(page: Page<S>, mapper: (S) -> T): PageEnvelope<T> = PageEnvelope(
            content = page.content.map(mapper),
            page = PageInfo(
                number = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
            ),
        )
    }
}
