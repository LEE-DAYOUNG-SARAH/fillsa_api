package store.fillsa.fillsa_api.common.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

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
        fun <T, R> from(page: Page<T>, responseMapper: (T) -> R): PageResponse<R> {
            return PageResponse(
                content = page.content.map(responseMapper),
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number
            )
        }
    }
}