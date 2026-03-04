package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "motor_data")
class MotorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    var mode: MotorMode? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    var direction: Direction? = null

    @Column(name = "left_speed")
    var leftSpeed: Int? = null

    @Column(name = "right_speed")
    var rightSpeed: Int? = null

    @Column(name = "ultrasonic_cm")
    var ultrasonicCm: Float? = null

    @Column(name = "ir_detected")
    var irDetected: Boolean? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    var source: Source? = null

    @Column(name = "recorded_at", nullable = false)
    var recordedAt: LocalDateTime = LocalDateTime.now()

    enum class MotorMode {
        TRACK, STOP, MANUAL
    }

    enum class Direction {
        FORWARD, BACKWARD, LEFT, RIGHT, STOP
    }

    enum class Source {
        USER_APP, AUTO, SCHEDULED
    }

    override fun toString(): String =
        "MotorData(id=$id, productId=${product.id}, mode=$mode, direction=$direction)"
}