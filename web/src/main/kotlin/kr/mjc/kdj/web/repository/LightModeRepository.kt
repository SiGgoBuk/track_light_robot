package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface LightModeRepository : JpaRepository<LightMode, Long> {
    fun findByProductId(productId: Long): LightMode?
}