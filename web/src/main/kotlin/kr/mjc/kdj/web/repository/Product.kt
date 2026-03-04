package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "product_number", nullable = false, unique = true, length = 100)
    lateinit var productNumber: String

    @Column(length = 50)
    var name: String? = null

    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Column(name = "registered_at", nullable = false)
    var registeredAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "ip_address", length = 100)
    var ipAddress: String? = null

    override fun toString(): String =
        "Product(id=$id, productNumber='$productNumber', name='$name', ipAddress='$ipAddress')"
}