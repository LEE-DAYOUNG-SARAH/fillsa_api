package com.fillsa.app.common.cache

import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * 캐시 무효화를 "지금 한 번 + 커밋 직후 한 번" 실행한다.
 *
 * 지금만 지우면, 커밋 전에 들어온 다른 요청이 아직 커밋되지 않은 옛 값을 DB 에서 읽어 캐시에 다시 넣을 수 있다.
 * 커밋 직후에 한 번 더 지워 그 틈을 닫는다. 트랜잭션 밖에서 호출되면 즉시 1회만 실행한다.
 */
fun evictNowAndAfterCommit(evict: () -> Unit) {
    evict()
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() = evict()
        })
    }
}
