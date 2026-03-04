package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

interface PendingUserRepository : JpaRepository<PendingUser, Long> {

    fun findByUsername(username: String): PendingUser?

    fun findByVerificationCode(code: String): PendingUser?

    fun existsByUsername(username: String): Boolean

    @Modifying
    @Transactional
    fun deleteByUsername(username: String)
}