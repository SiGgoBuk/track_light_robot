package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "motor_mode")
class MotorMode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Column(nullable = false, length = 20)
    lateinit var mode: String

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PreUpdate
    protected fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun toString(): String =
        "MotorMode(id=$id, mode='$mode', updatedAt=$updatedAt)"
}