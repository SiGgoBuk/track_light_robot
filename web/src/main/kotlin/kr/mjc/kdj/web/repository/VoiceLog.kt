package kr.mjc.kdj.web.repository

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "voice_log")
class VoiceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @Column(name = "input_text", nullable = false, length = 255)
    lateinit var inputText: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_action_id")
    var matchedAction: Action? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    override fun toString(): String =
        "VoiceLog(id=$id, inputText='$inputText', matchedActionId=${matchedAction?.id})"
}