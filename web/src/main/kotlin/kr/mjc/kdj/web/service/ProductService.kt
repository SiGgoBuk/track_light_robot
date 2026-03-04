package kr.mjc.kdj.web.service

import kr.mjc.kdj.web.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val lightDataRepository: LightDataRepository,
    private val motorDataRepository: MotorDataRepository,
    private val userRepository: UserRepository
) {

    /**
     * 사용자의 제품 목록 조회
     */
    fun getUserProducts(userId: Long): List<Product> {
        return productRepository.findByUserId(userId)
    }

    /**
     * 제품 상세 정보 조회
     */
    fun getProduct(productId: Long): Product {
        return productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("존재하지 않는 제품입니다.") }
    }

    /**
     * 제품의 조명 데이터 조회 (페이징 + 정렬)
     */
    fun getLightData(productId: Long, pageable: Pageable, sort: String = "desc"): Page<LightData> {
        return if (sort == "asc") {
            lightDataRepository.findByProductIdOrderByRecordedAtAsc(productId, pageable)
        } else {
            lightDataRepository.findByProductIdOrderByRecordedAtDesc(productId, pageable)
        }
    }

    /**
     * 제품의 최근 조명 데이터 10개 조회
     */
    fun getRecentLightData(productId: Long): List<LightData> {
        return lightDataRepository.findTop10ByProductIdOrderByRecordedAtDesc(productId)
    }

    /**
     * 제품의 모터 데이터 조회 (페이징 + 정렬)
     */
    fun getMotorData(productId: Long, pageable: Pageable, sort: String = "desc"): Page<MotorData> {
        return if (sort == "asc") {
            motorDataRepository.findByProductIdOrderByRecordedAtAsc(productId, pageable)
        } else {
            motorDataRepository.findByProductIdOrderByRecordedAtDesc(productId, pageable)
        }
    }

    /**
     * 제품의 최근 모터 데이터 10개 조회
     */
    fun getRecentMotorData(productId: Long): List<MotorData> {
        return motorDataRepository.findTop10ByProductIdOrderByRecordedAtDesc(productId)
    }

    /**
     * 경로 추적용: 제품의 최근 모터 데이터 N개 조회
     */
    fun getPathMotorData(productId: Long, count: Int): List<MotorData> {
        return motorDataRepository.findByProductIdOrderByRecordedAtDesc(
            productId,
            org.springframework.data.domain.PageRequest.of(0, count)
        ).content
    }

    /**
     * 제품 등록 (기존 제품을 내 계정에 연결)
     */
    fun registerProduct(productNumber: String, name: String?, userId: Long, ipAddress: String?): Product {
        // 제품번호로 기존 제품 찾기
        val product = productRepository.findByProductNumber(productNumber)
            ?: throw IllegalArgumentException("존재하지 않는 제품 번호입니다.")

        // 이미 다른 사용자에게 등록되어 있는지 확인
        if (product.user != null) {
            throw IllegalArgumentException("이미 다른 사용자에게 등록된 제품입니다.")
        }

        // 사용자 조회
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        // 제품을 내 계정에 연결
        product.user = user
        if (name != null) {
            product.name = name
        }

        return productRepository.save(product)
    }

    /**
     * 제품 해제 (내 계정에서 연결 해제, 제품 자체는 삭제 안 함)
     */
    fun unregisterProduct(productId: Long, userId: Long) {
        val product = getProduct(productId)

        if (product.user?.id != userId) {
            throw IllegalArgumentException("권한이 없습니다.")
        }

        // 제품을 삭제하지 않고 user_id만 NULL로 설정
        product.user = null
        productRepository.save(product)
    }
}