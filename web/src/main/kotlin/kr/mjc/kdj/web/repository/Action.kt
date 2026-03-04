package kr.mjc.kdj.web.repository

import jakarta.persistence.*

@Entity
@Table(name = "action")
class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false, unique = true, length = 50)
    lateinit var code: String

    @Column(nullable = false, length = 50)
    lateinit var name: String

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    override fun toString(): String =
        "Action(id=$id, code='$code', name='$name')"
}