package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "led_schedule")
class LedSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Column(name = "scheduled_time", nullable = false)
    lateinit var scheduledTime: LocalDateTime

    @Column(name = "led_color_r", nullable = false)
    var ledColorR: Int = 0

    @Column(name = "led_color_g", nullable = false)
    var ledColorG: Int = 0

    @Column(name = "led_color_b", nullable = false)
    var ledColorB: Int = 0

    @Column(nullable = false)
    var executed: Boolean = false

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    override fun toString(): String =
        "LedSchedule(id=$id, scheduledTime=$scheduledTime, RGB=($ledColorR,$ledColorG,$ledColorB), executed=$executed)"
}