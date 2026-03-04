package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "light_data")
class LightData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Column(name = "brightness_sensor_value")
    var brightnessSensorValue: Int? = null

    @Column(name = "potentiometer_value")
    var potentiometerValue: Int? = null

    @Column(name = "led_color_r")
    var ledColorR: Int? = null

    @Column(name = "led_color_g")
    var ledColorG: Int? = null

    @Column(name = "led_color_b")
    var ledColorB: Int? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "brightness_mode")
    var brightnessMode: BrightnessMode? = null

    @Column(name = "recorded_at", nullable = false)
    var recordedAt: LocalDateTime = LocalDateTime.now()

    enum class BrightnessMode {
        AUTO, MANUAL, APP
    }

    override fun toString(): String =
        "LightData(id=$id, productId=${product.id}, mode=$brightnessMode, RGB=($ledColorR,$ledColorG,$ledColorB))"
}