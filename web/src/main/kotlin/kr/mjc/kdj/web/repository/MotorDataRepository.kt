package kr.mjc.kdj.web.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface MotorDataRepository : JpaRepository<MotorData, Long> {

    fun findByProductIdOrderByRecordedAtDesc(productId: Long, pageable: Pageable): Page<MotorData>

    fun findByProductIdOrderByRecordedAtAsc(productId: Long, pageable: Pageable): Page<MotorData>

    fun findTop10ByProductIdOrderByRecordedAtDesc(productId: Long): List<MotorData>
}