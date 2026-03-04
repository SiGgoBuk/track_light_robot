package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface VoiceLogRepository : JpaRepository<VoiceLog, Long> {
    fun findByProductIdOrderByCreatedAtDesc(productId: Long): List<VoiceLog>
    fun findTop50ByProductIdOrderByCreatedAtDesc(productId: Long): List<VoiceLog>
}