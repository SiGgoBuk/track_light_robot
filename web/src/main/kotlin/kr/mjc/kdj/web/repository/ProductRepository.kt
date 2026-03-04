package kr.mjc.kdj.web.repository

import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {

    fun findByProductNumber(productNumber: String): Product?

    fun findByUserId(userId: Long): List<Product>

    fun existsByProductNumber(productNumber: String): Boolean
}