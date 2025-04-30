package com.fillsa.fillsa_api.domain.notice.entity

import com.fillsa.fillsa_api.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "notices")
class Notice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val noticeSeq: Long = 0L,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var content: String
): BaseEntity()