package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "slam_track_data")
class SlamTrackData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Column(name = "pos_x", nullable = false)
    var posX: Float = 0f

    @Column(name = "pos_y", nullable = false)
    var posY: Float = 0f

    @Column(name = "pos_z")
    var posZ: Float = 0f

    @Column(name = "recorded_at")
    var recordedAt: LocalDateTime = LocalDateTime.now()

    override fun toString(): String =
        "SlamTrackData(id=$id, position=($posX, $posY, $posZ), recordedAt=$recordedAt)"
}