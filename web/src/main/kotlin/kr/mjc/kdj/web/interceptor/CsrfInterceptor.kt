package kr.mjc.kdj.web.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import kr.mjc.kdj.web.generateRandomString
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.HandlerInterceptor

/**
 * csrf 토큰을 생성하고 검증하는 인터셉터
 * req.method == "GET" 일 때 csrf 토큰을 생성해서 세션에 저장한다.
 * erq.method == "POST" 일 때 세션의 토큰과 폼에서 올라온 토큰을 비교한다.
 */
@Component
class CsrfInterceptor : HandlerInterceptor {
    companion object {
        private const val CSRF = "csrf"
    }

    override fun preHandle(req: HttpServletRequest, resp: HttpServletResponse,
                           handler: Any): Boolean {
        val session : HttpSession = req.session

        if(req.method == "GET") { // GET일 때 csrf token 생성해서 세션에 저장
            val csrfToken = generateRandomString(32)
            session.setAttribute(CSRF, csrfToken)
            return true
        } else if (req.method == "POST") { // POST일 때 폼의 csrf token 검증
            val csrfToken = session.getAttribute(CSRF) as? String
            if(csrfToken != null) {
                session.removeAttribute(CSRF) // 한번 사용한 토큰 삭제
                val _csrf = req.getParameter("_csrf")
                if (csrfToken == _csrf) return true
            }
        }
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "CSRF 에러")
    }
}