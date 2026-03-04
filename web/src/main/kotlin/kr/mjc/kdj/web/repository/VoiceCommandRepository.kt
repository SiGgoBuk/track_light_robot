package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface VoiceCommandRepository : JpaRepository<VoiceCommand, Long> {
    fun findByProductId(productId: Long): List<VoiceCommand>
}