package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "product_setting")
class ProductSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Column(name = "setting_key", nullable = false, length = 50)
    lateinit var settingKey: String

    @Column(name = "setting_value", nullable = false, length = 100)
    lateinit var settingValue: String

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PreUpdate
    protected fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun toString(): String =
        "ProductSetting(id=$id, key='$settingKey', value='$settingValue')"
}