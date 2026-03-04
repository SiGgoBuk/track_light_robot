package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "user")
class User : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false, length = 50)
    lateinit var username: String

    @Column(length = 255)
    var password: String? = null

    @Column(name = "first_name", nullable = false, length = 20)
    lateinit var firstName: String

    @Column(name = "date_joined", nullable = false)
    var dateJoined: LocalDateTime = LocalDateTime.now()

    @Column(name = "last_login", nullable = false)
    var lastLogin: LocalDateTime = LocalDateTime.now()

    @Column(name = "email_verified")
    var emailVerified: Boolean = false

    @Column(name = "verification_code", length = 64)
    var verificationCode: String? = null

    override fun toString(): String =
        "User(id=$id, username='$username', firstName='$firstName', emailVerified=$emailVerified)"
}