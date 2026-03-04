package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "motor_control")
class MotorControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Column(name = "command_type", nullable = false, length = 20)
    lateinit var commandType: String

    @Column(nullable = false)
    var executed: Boolean = false

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "executed_at")
    var executedAt: LocalDateTime? = null

    override fun toString(): String =
        "MotorControl(id=$id, commandType='$commandType', executed=$executed)"
}