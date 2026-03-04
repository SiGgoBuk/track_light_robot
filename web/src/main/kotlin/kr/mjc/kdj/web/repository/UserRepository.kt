package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface UserRepository : JpaRepository<User, Long> {

    fun findByUsername(username: String): User?

    fun existsByUsername(username: String): Boolean

    fun findByVerificationCode(code: String): User?

    @Modifying
    @Transactional
    @Query("update User set lastLogin=:lastLogin where id=:id")
    fun updateLastLogin(id: Long, lastLogin: LocalDateTime)

    @Modifying
    @Transactional
    @Query("update User set emailVerified=true, verificationCode=null where id=:id")
    fun verifyEmail(id: Long)

    @Modifying
    @Transactional
    @Query("update User set password=:password, verificationCode=null where id=:id")
    fun updatePassword(id: Long, password: String)
}