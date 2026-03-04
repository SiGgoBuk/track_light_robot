package kr.mjc.kdj.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // 개발 단계에서는 비활성화
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 없이 접근 가능한 페이지
                    .requestMatchers(
                        "/",
                        "/login",
                        "/signup",
                        "/verify",
                        "/forgot-password",
                        "/reset-password",
                        "/auth/**",
                        "/api/**",           // API 엔드포인트 추가
                        "/api-test.html",    // API 테스트 페이지 추가
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/static/**",        // Static 리소스 전체
                        "/error"
                    ).permitAll()
                    // 나머지는 모두 인증 필요 (나중에 활성화)
                    .anyRequest().permitAll() // 개발 단계에서는 전부 허용
            }
            .formLogin { it.disable() } // 커스텀 로그인 사용
            .logout { it.disable() } // 커스텀 로그아웃 사용

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}