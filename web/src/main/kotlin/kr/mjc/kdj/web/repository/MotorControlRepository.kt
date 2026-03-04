package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface MotorControlRepository : JpaRepository<MotorControl, Long> {
    fun findByProductIdAndExecutedFalseOrderByCreatedAtDesc(productId: Long): List<MotorControl>
}