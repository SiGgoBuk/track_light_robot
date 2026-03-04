package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pending_user")
class PendingUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false, unique = true, length = 50)
    lateinit var username: String

    @Column(nullable = false, length = 60)
    lateinit var password: String

    @Column(name = "first_name", nullable = false, length = 20)
    lateinit var firstName: String

    @Column(name = "verification_code", nullable = false, length = 255)
    lateinit var verificationCode: String

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    override fun toString(): String =
        "PendingUser(id=$id, username='$username', firstName='$firstName')"
}