package com.fillsa.fillsa_api.domain.members.quote.service.useCase

import com.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface MemberQuoteDataSyncUseCase {
    /**
     *  사용자 명언 데이터 연동
     */
    fun syncData(member: Member, request: List<LoginRequest.SyncData>)
}