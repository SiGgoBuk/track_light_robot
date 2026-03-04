package kr.mjc.kdj.web.service

import kr.mjc.kdj.web.generateRandomString
import kr.mjc.kdj.web.repository.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val pendingUserRepository: PendingUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val mailSender: JavaMailSender
) {

    /**
     * 회원가입 요청 - 이메일 인증 필요
     */
    @Transactional
    fun requestSignup(username: String, password: String, firstName: String): String {
        // 이미 인증된 사용자인지 확인
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("이미 가입된 이메일입니다.")
        }

        // 인증 코드 생성
        val verificationCode = generateRandomString(32)

        // 기존 pending_user가 있으면 업데이트, 없으면 새로 생성
        val pendingUser = pendingUserRepository.findByUsername(username) ?: PendingUser()

        pendingUser.apply {
            this.username = username
            this.password = passwordEncoder.encode(password)
            this.firstName = firstName
            this.verificationCode = verificationCode
            this.createdAt = java.time.LocalDateTime.now()
        }

        pendingUserRepository.save(pendingUser)

        // 인증 이메일 발송
        sendVerificationEmail(username, verificationCode)

        return verificationCode
    }

    /**
     * 이메일 인증 완료
     */
    @Transactional
    fun verifyEmail(code: String): User {
        val pendingUser = pendingUserRepository.findByVerificationCode(code)
            ?: throw IllegalArgumentException("유효하지 않은 인증 코드입니다.")

        // User 테이블로 이동
        val user = User().apply {
            username = pendingUser.username
            password = pendingUser.password
            firstName = pendingUser.firstName
            emailVerified = true
            dateJoined = LocalDateTime.now()
            lastLogin = LocalDateTime.now()
        }
        userRepository.save(user)

        // PendingUser 삭제
        pendingUserRepository.delete(pendingUser)

        return user
    }

    /**
     * 로그인
     */
    fun login(username: String, password: String): User? {
        val user = userRepository.findByUsername(username) ?: return null

        if (!passwordEncoder.matches(password, user.password)) {
            return null
        }

        // 마지막 로그인 시간 업데이트
        userRepository.updateLastLogin(user.id, LocalDateTime.now())

        return user
    }

    /**
     * 비밀번호 찾기 - 재설정 링크 이메일 발송
     */
    @Transactional
    fun requestPasswordReset(username: String): String {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다.")

        val resetCode = generateRandomString(32)
        user.verificationCode = resetCode
        userRepository.save(user)

        // 비밀번호 재설정 이메일 발송
        sendPasswordResetEmail(username, resetCode)

        return resetCode
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    fun resetPassword(code: String, newPassword: String) {
        val user = userRepository.findByVerificationCode(code)
            ?: throw IllegalArgumentException("유효하지 않은 코드입니다.")

        val encodedPassword = passwordEncoder.encode(newPassword)
        userRepository.updatePassword(user.id, encodedPassword)
    }

    /**
     * 비밀번호 변경 (로그인 상태)
     */
    @Transactional
    fun changePassword(userId: Long, currentPassword: String, newPassword: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.")
        }

        val encodedPassword = passwordEncoder.encode(newPassword)
        userRepository.updatePassword(user.id, encodedPassword)
    }

    /**
     * 계정 삭제
     */
    @Transactional
    fun deleteAccount(userId: Long, password: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("비밀번호가 올바르지 않습니다.")
        }

        userRepository.deleteById(userId)
    }

    /**
     * 인증 이메일 발송
     */
    private fun sendVerificationEmail(email: String, code: String) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            subject = "조명로봇 시스템 - 이메일 인증"
            text = """
                안녕하세요!
                
                아래 링크를 클릭하여 이메일 인증을 완료해주세요:
                https://lightproject.duckdns.org/verify?code=$code
                
                감사합니다.
            """.trimIndent()
        }
        mailSender.send(message)
    }

    /**
     * 비밀번호 재설정 이메일 발송
     */
    private fun sendPasswordResetEmail(email: String, code: String) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            subject = "조명로봇 시스템 - 비밀번호 재설정"
            text = """
                안녕하세요!
                
                아래 링크를 클릭하여 비밀번호를 재설정해주세요:
                https://lightproject.duckdns.org/reset-password?code=$code
                
                감사합니다.
            """.trimIndent()
        }
        mailSender.send(message)
    }
}