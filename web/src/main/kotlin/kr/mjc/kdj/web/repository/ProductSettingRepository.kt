package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface ProductSettingRepository : JpaRepository<ProductSettings, Long> {
    fun findByProductId(productId: Long): List<ProductSettings>
    fun findByProductIdAndSettingKey(productId: Long, settingKey: String): ProductSettings?
}