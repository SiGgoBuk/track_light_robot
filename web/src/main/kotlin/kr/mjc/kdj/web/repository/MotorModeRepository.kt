package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface MotorModeRepository : JpaRepository<MotorMode, Long> {
    fun findByProductId(productId: Long): MotorMode?
}