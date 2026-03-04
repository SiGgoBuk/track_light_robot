package kr.mjc.kdj.web.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface LightDataRepository : JpaRepository<LightData, Long> {

    fun findByProductIdOrderByRecordedAtDesc(productId: Long, pageable: Pageable): Page<LightData>

    fun findByProductIdOrderByRecordedAtAsc(productId: Long, pageable: Pageable): Page<LightData>

    fun findTop10ByProductIdOrderByRecordedAtDesc(productId: Long): List<LightData>
}