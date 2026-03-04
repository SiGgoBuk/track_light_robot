package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "light_mode")
class LightMode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var mode: Mode = Mode.AUTO

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()

    enum class Mode {
        AUTO, MANUAL, APP
    }

    @PreUpdate
    protected fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun toString(): String =
        "LightMode(id=$id, mode=$mode, updatedAt=$updatedAt)"
}