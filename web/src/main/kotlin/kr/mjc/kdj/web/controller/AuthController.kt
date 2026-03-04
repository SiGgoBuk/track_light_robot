package kr.mjc.kdj.web.controller

import jakarta.servlet.http.HttpSession
import kr.mjc.kdj.web.repository.User
import kr.mjc.kdj.web.service.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttribute
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AuthController(private val userService: UserService) {

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    fun loginPage(): String = "auth/login"

    /**
     * 로그인 처리
     */
    @PostMapping("/login")
    fun login(
        @RequestParam username: String,
        @RequestParam password: String,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            val user = userService.login(username, password)
            if (user != null) {
                session.setAttribute("user", user)
                "redirect:/dashboard"
            } else {
                redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.")
                "redirect:/login"
            }
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/login"
        }
    }

    /**
     * 로그아웃
     */
    @GetMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/login"
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    fun signupPage(): String = "auth/signup"

    /**
     * 회원가입 요청
     */
    @PostMapping("/signup")
    fun signup(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam firstName: String,
        model: Model
    ): String {
        return try {
            userService.requestSignup(username, password, firstName)
            model.addAttribute("message", "인증 이메일이 발송되었습니다. 이메일을 확인해주세요.")
            "auth/signup-success"
        } catch (e: Exception) {
            model.addAttribute("error", e.message)
            "auth/signup"
        }
    }

    /**
     * 이메일 인증 처리
     */
    @GetMapping("/verify")
    fun verify(
        @RequestParam code: String,
        session: HttpSession,
        model: Model
    ): String {
        return try {
            val user = userService.verifyEmail(code)
            session.setAttribute("user", user)
            "redirect:/dashboard"
        } catch (e: Exception) {
            model.addAttribute("error", e.message)
            "auth/verify-error"
        }
    }

    /**
     * 비밀번호 찾기 페이지
     */
    @GetMapping("/forgot-password")
    fun forgotPasswordPage(): String = "auth/forgot-password"

    /**
     * 비밀번호 재설정 요청
     */
    @PostMapping("/forgot-password")
    fun forgotPassword(
        @RequestParam username: String,
        model: Model
    ): String {
        return try {
            userService.requestPasswordReset(username)
            model.addAttribute("message", "비밀번호 재설정 이메일이 발송되었습니다.")
            "auth/forgot-password-success"
        } catch (e: Exception) {
            model.addAttribute("error", e.message)
            "auth/forgot-password"
        }
    }

    /**
     * 비밀번호 재설정 페이지
     */
    @GetMapping("/reset-password")
    fun resetPasswordPage(@RequestParam code: String, model: Model): String {
        model.addAttribute("code", code)
        return "auth/reset-password"
    }

    /**
     * 비밀번호 재설정 처리
     */
    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestParam code: String,
        @RequestParam newPassword: String,
        model: Model
    ): String {
        return try {
            userService.resetPassword(code, newPassword)
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.")
            "auth/reset-password-success"
        } catch (e: Exception) {
            model.addAttribute("error", e.message)
            model.addAttribute("code", code)
            "auth/reset-password"
        }
    }

    /**
     * 비밀번호 변경 페이지 (로그인 상태)
     */
    @GetMapping("/change-password")
    fun changePasswordPage(@SessionAttribute(required = false) user: User?): String {
        if (user == null) {
            return "redirect:/login"
        }
        return "auth/change-password"
    }

    /**
     * 비밀번호 변경 처리
     */
    @PostMapping("/change-password")
    fun changePassword(
        @RequestParam currentPassword: String,
        @RequestParam newPassword: String,
        @SessionAttribute(required = false) user: User?,
        redirectAttributes: RedirectAttributes
    ): String {
        if (user == null) {
            return "redirect:/login"
        }

        return try {
            userService.changePassword(user.id, currentPassword, newPassword)
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.")
            "redirect:/mypage"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/change-password"
        }
    }

    /**
     * 계정 삭제 페이지
     */
    @GetMapping("/delete-account")
    fun deleteAccountPage(@SessionAttribute(required = false) user: User?): String {
        if (user == null) {
            return "redirect:/login"
        }
        return "auth/delete-account"
    }

    /**
     * 계정 삭제 처리
     */
    @PostMapping("/delete-account")
    fun deleteAccount(
        @RequestParam password: String,
        @SessionAttribute(required = false) user: User?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        if (user == null) {
            return "redirect:/login"
        }

        return try {
            userService.deleteAccount(user.id, password)
            session.invalidate()
            redirectAttributes.addFlashAttribute("message", "계정이 삭제되었습니다.")
            "redirect:/login"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/delete-account"
        }
    }
}