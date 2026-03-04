package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "voice_command")
class VoiceCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Column(name = "input_text", nullable = false, length = 100)
    lateinit var inputText: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    lateinit var action: Action

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    override fun toString(): String =
        "VoiceCommand(id=$id, inputText='$inputText', actionId=${action.id})"
}