package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface LightControlRepository : JpaRepository<LightControl, Long> {
    fun findByProductIdAndExecutedFalseOrderByCreatedAtDesc(productId: Long): List<LightControl>
}