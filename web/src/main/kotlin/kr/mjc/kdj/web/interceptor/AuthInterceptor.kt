package kr.mjc.kdj.web.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/** 로그인 여부를 체크하는 인터셉터 */
@Component
class AuthInterceptor : HandlerInterceptor {
    override fun preHandle(req: HttpServletRequest, resp: HttpServletResponse,
                           handler: Any ) : Boolean {
        return when(req.session.getAttribute("user")) {
            null -> { // 로그인 안했을 경우 로그인 화면으로
                resp.sendRedirect("${req.contextPath}/user/login")
                false
            }
            else -> true // 로그인 했을 경우 그대로 진행
        }
    }
}