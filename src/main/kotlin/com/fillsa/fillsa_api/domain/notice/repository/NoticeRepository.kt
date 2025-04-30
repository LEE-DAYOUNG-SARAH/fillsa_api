package com.fillsa.fillsa_api.domain.notice.repository

import com.fillsa.fillsa_api.domain.notice.entity.Notice
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NoticeRepository: JpaRepository<Notice, Long> {
    @Query("""
        select nt
        from Notice nt
        order by nt.createdAt desc
    """)
    fun findByPageable(pageable: Pageable): Page<Notice>
}