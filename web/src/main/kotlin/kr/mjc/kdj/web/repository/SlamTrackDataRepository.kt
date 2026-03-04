package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface SlamTrackDataRepository : JpaRepository<SlamTrackData, Long> {
    fun findByProductIdOrderByRecordedAtDesc(productId: Long): List<SlamTrackData>
    fun findTop100ByProductIdOrderByRecordedAtDesc(productId: Long): List<SlamTrackData>
}