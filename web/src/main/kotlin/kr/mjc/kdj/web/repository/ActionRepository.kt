package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface ActionRepository : JpaRepository<Action, Long> {
    fun findByCode(code: String): Action?
}