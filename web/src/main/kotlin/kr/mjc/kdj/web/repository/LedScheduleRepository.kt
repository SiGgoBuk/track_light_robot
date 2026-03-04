package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface LedScheduleRepository : JpaRepository<LedSchedule, Long> {
    fun findByProductIdAndExecutedFalseOrderByScheduledTimeAsc(productId: Long): List<LedSchedule>
}