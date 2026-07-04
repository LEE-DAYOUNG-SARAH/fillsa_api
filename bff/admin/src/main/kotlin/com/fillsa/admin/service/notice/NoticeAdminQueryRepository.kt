package com.fillsa.admin.service.notice

import com.fillsa.service.notice.Notice
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface NoticeAdminQueryRepository : JpaRepository<Notice, Long> {

    /**
     * 어드민 공지 목록. keyword 는 제목 부분일치. null 이면 무시된다.
     */
    @Query(
        """
        select nt
        from Notice nt
        where :keyword is null
            or lower(nt.title) like lower(concat('%', :keyword, '%'))
        """,
    )
    fun search(
        @Param("keyword") keyword: String?,
        pageable: Pageable,
    ): Page<Notice>
}
