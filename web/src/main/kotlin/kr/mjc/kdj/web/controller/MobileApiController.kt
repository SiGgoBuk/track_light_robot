package kr.mjc.kdj.web.controller

import kr.mjc.kdj.web.repository.*
import kr.mjc.kdj.web.service.ProductService
import kr.mjc.kdj.web.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
class MobileApiController(
    private val userService: UserService,
    private val productService: ProductService,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val lightDataRepository: LightDataRepository,
    private val motorDataRepository: MotorDataRepository,
    private val productSettingRepository: ProductSettingRepository
) {

    // ==================== User API ====================

    /**
     * 로그인
     * POST /api/user/login
     * Request: { "username": "user@email.com", "password": "1234" }
     * Response: { "success": true, "data": 1, "message": "로그인 성공" }
     */
    @PostMapping("/user/login")
    fun login(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any?>> {
        println("🔵 [LOGIN] 요청 받음: $request")

        val username = request["username"] ?: return ResponseEntity.badRequest()
            .body(mapOf("success" to false, "message" to "Username is required", "data" to null))

        val password = request["password"] ?: return ResponseEntity.badRequest()
            .body(mapOf("success" to false, "message" to "Password is required", "data" to null))

        return try {
            println("🔵 [LOGIN] userService.login() 호출 - username: $username")
            val user = userService.login(username, password)

            println("🔵 [LOGIN] user 객체: $user")
            println("🔵 [LOGIN] user != null: ${user != null}")

            if (user != null) {
                val userId = user.id
                println("🔵 [LOGIN] user.id = $userId (타입: ${userId::class.java})")

                if (userId == 0L) {
                    println("⚠️ [LOGIN] userId가 0임! DB에서 재조회 시도")
                    val dbUser = userRepository.findByUsername(username)
                    println("🔵 [LOGIN] DB 재조회 결과: $dbUser")

                    if (dbUser != null && dbUser.id > 0) {
                        val response = mapOf(
                            "success" to true,
                            "data" to dbUser.id,       // data 필드
                            "userId" to dbUser.id,     // ✅ userId 필드도 추가
                            "message" to "로그인 성공"
                        )
                        println("✅ [LOGIN] 응답 (재조회): $response")
                        ResponseEntity.ok(response)
                    } else {
                        val response = mapOf(
                            "success" to false,
                            "data" to null,
                            "message" to "사용자 ID를 가져올 수 없습니다."
                        )
                        println("❌ [LOGIN] 응답 (재조회 실패): $response")
                        ResponseEntity.ok(response)
                    }
                } else {
                    val response = mapOf(
                        "success" to true,
                        "data" to userId,      // data 필드
                        "userId" to userId,    // ✅ userId 필드도 추가 (하위 호환성)
                        "message" to "로그인 성공"
                    )
                    println("✅ [LOGIN] 응답 (정상): $response")
                    println("✅ [LOGIN] data 필드 타입: ${response["data"]?.javaClass}")
                    ResponseEntity.ok(response)
                }
            } else {
                val response = mapOf(
                    "success" to false,
                    "data" to null,
                    "message" to "이메일 또는 비밀번호가 올바르지 않습니다."
                )
                println("❌ [LOGIN] 응답 (인증 실패): $response")
                ResponseEntity.ok(response)
            }
        } catch (e: Exception) {
            println("❌ [LOGIN] 예외 발생: ${e.message}")
            e.printStackTrace()
            val response = mapOf(
                "success" to false,
                "data" to null,
                "message" to (e.message ?: "로그인 중 오류가 발생했습니다.")
            )
            println("❌ [LOGIN] 응답 (예외): $response")
            ResponseEntity.ok(response)
        }
    }

    /**
     * 회원가입
     * POST /api/user/signup
     * Request: { "username": "user@email.com", "password": "1234", "firstName": "홍길동" }
     * Response: { "success": true, "message": "인증 이메일이 발송되었습니다.", "data": null }
     */
    @PostMapping("/user/signup")
    fun signup(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any?>> {
        val username = request["username"] ?: return ResponseEntity.badRequest()
            .body(mapOf("success" to false, "message" to "Username is required", "data" to null))

        val password = request["password"] ?: return ResponseEntity.badRequest()
            .body(mapOf("success" to false, "message" to "Password is required", "data" to null))

        val firstName = request["firstName"] ?: return ResponseEntity.badRequest()
            .body(mapOf("success" to false, "message" to "First name is required", "data" to null))

        return try {
            userService.requestSignup(username, password, firstName)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "인증 이메일이 발송되었습니다. 이메일을 확인해주세요.",
                "data" to null
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "success" to false,
                "message" to (e.message ?: "회원가입 중 오류가 발생했습니다."),
                "data" to null
            ))
        }
    }

    /**
     * 사용자 프로필 조회
     * GET /api/user/profile?userId=1
     * Response: { "id": 1, "username": "user@email.com", "firstName": "홍길동" }
     */
    @GetMapping("/user/profile")
    fun getUserProfile(@RequestParam userId: Long): ResponseEntity<Map<String, Any>> {
        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "User not found"))

        return ResponseEntity.ok(mapOf(
            "id" to user.id,
            "username" to user.username,
            "firstName" to user.firstName
        ))
    }

    // ==================== Product API ====================

    /**
     * 사용자의 제품 목록 조회
     * GET /api/product/list?userId=1
     * Response: [{ "id": 1, "name": "거실 로봇", "productNumber": "ABC123", ... }]
     */
    @GetMapping("/product/list")
    fun getProductList(@RequestParam userId: Long): ResponseEntity<List<Map<String, Any?>>> {
        val products = productRepository.findByUserId(userId)

        val result = products.map { product ->
            mapOf(
                "id" to product.id,
                "name" to product.name,
                "productNumber" to product.productNumber,
                "ipAddress" to product.ipAddress,
                "registeredAt" to product.registeredAt.toString()
            )
        }

        return ResponseEntity.ok(result)
    }

    // ==================== Analysis API ====================

    /**
     * 제품 데이터 요약 통계
     * GET /api/analysis/summary?productId=1
     * Response: {
     *   "avgBrightness": 512,
     *   "avgSensor": 400,
     *   "mostUsedMode": "AUTO",
     *   "avgLeftSpeed": 180,
     *   "avgRightSpeed": 180,
     *   "mostUsedMotorMode": "TRACK",
     *   "avgUltrasonic": 25
     * }
     */
    @GetMapping("/analysis/summary")
    fun getAnalysisSummary(@RequestParam productId: Long): ResponseEntity<Map<String, Any>> {
        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "Product not found"))

        // 조명 데이터 통계
        val lightDataList = lightDataRepository.findTop10ByProductIdOrderByRecordedAtDesc(productId)

        val avgBrightness = if (lightDataList.isNotEmpty()) {
            val totalRgb = lightDataList.mapNotNull {
                (it.ledColorR ?: 0) + (it.ledColorG ?: 0) + (it.ledColorB ?: 0)
            }
            if (totalRgb.isNotEmpty()) totalRgb.average().toInt() else 0
        } else 0

        val avgSensor = if (lightDataList.isNotEmpty()) {
            lightDataList.mapNotNull { it.brightnessSensorValue }.average().toInt()
        } else 0

        val mostUsedMode = if (lightDataList.isNotEmpty()) {
            lightDataList
                .mapNotNull { it.brightnessMode?.name }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: "AUTO"
        } else "AUTO"

        // 모터 데이터 통계
        val motorDataList = motorDataRepository.findTop10ByProductIdOrderByRecordedAtDesc(productId)

        val avgLeftSpeed = if (motorDataList.isNotEmpty()) {
            motorDataList.mapNotNull { it.leftSpeed }.average().toInt()
        } else 0

        val avgRightSpeed = if (motorDataList.isNotEmpty()) {
            motorDataList.mapNotNull { it.rightSpeed }.average().toInt()
        } else 0

        val mostUsedMotorMode = if (motorDataList.isNotEmpty()) {
            motorDataList
                .mapNotNull { it.mode?.name }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: "STOP"
        } else "STOP"

        val avgUltrasonic = if (motorDataList.isNotEmpty()) {
            motorDataList.mapNotNull { it.ultrasonicCm }.average().toInt()
        } else 0

        return ResponseEntity.ok(mapOf(
            "avgBrightness" to avgBrightness,
            "avgSensor" to avgSensor,
            "mostUsedMode" to mostUsedMode,
            "avgLeftSpeed" to avgLeftSpeed,
            "avgRightSpeed" to avgRightSpeed,
            "mostUsedMotorMode" to mostUsedMotorMode,
            "avgUltrasonic" to avgUltrasonic
        ))
    }

    // ==================== Product Settings API ====================

    /**
     * 제품 설정 저장/업데이트
     * POST /api/product-settings
     * Request: { "productId": 1, "settingKey": "brightness_threshold", "settingValue": "512" }
     * Response: { "success": true, "message": "Setting saved" }
     */
    @PostMapping("/product-settings")
    fun saveOrUpdateProductSetting(@RequestBody request: Map<String, Any>): ResponseEntity<Map<String, Any>> {
        val productId = (request["productId"] as? Number)?.toLong()
            ?: return ResponseEntity.badRequest()
                .body(mapOf("success" to false, "message" to "Product ID is required"))

        val settingKey = request["settingKey"] as? String
            ?: return ResponseEntity.badRequest()
                .body(mapOf("success" to false, "message" to "Setting key is required"))

        val settingValue = request["settingValue"] as? String
            ?: return ResponseEntity.badRequest()
                .body(mapOf("success" to false, "message" to "Setting value is required"))

        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest()
                .body(mapOf("success" to false, "message" to "Product not found"))

        // 기존 설정이 있으면 업데이트, 없으면 새로 생성
        val setting = productSettingRepository.findByProductIdAndSettingKey(productId, settingKey)
            ?: ProductSettings().apply {
                this.product = product
                this.settingKey = settingKey
            }

        setting.settingValue = settingValue
        setting.updatedAt = LocalDateTime.now()
        productSettingRepository.save(setting)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Setting saved successfully",
            "settingKey" to settingKey,
            "settingValue" to settingValue
        ))
    }
}