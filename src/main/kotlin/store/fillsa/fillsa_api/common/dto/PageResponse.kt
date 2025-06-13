package store.fillsa.fillsa_api.common.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

data class PageResponse<T>(
    @Schema(description = "내용", required = true)
    val content: List<T> = listOf(),

    @Schema(description = "총 갯수", required = true)
    val totalElements: Long,

    @Schema(description = "총 페이지", required = true)
    val totalPages: Int,

    @Schema(description = "현재 페이지(0부터 시작)", required = true)
    val currentPage: Int
) {
    companion object {
        fun <T, R> fromPage(page: Page<T>, responseMapper: (T) -> R): PageResponse<R> {
            return PageResponse(
                content = page.content.map(responseMapper),
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number
            )
        }

        fun <T, R> fromList(
            list: List<T>,
            pageable: Pageable,
            responseMapper: (T) -> R
        ): PageResponse<R> {
            val totalElements = list.size.toLong()
            val totalPages = if (totalElements == 0L)
                1 else ((totalElements + pageable.pageSize - 1) / pageable.pageSize).toInt()

            return PageResponse(
                content = list.map(responseMapper),
                totalElements = totalElements,
                totalPages = totalPages,
                currentPage = pageable.pageNumber
            )
        }
    }
}