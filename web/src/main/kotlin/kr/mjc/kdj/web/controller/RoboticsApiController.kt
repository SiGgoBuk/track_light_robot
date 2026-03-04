package kr.mjc.kdj.web.controller

import kr.mjc.kdj.web.repository.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
class RoboticsApiController(
    private val productRepository: ProductRepository,
    private val motorModeRepository: MotorModeRepository,
    private val motorControlRepository: MotorControlRepository,
    private val motorDataRepository: MotorDataRepository,
    private val lightModeRepository: LightModeRepository,
    private val lightControlRepository: LightControlRepository,
    private val lightDataRepository: LightDataRepository,
    private val voiceCommandRepository: VoiceCommandRepository,
    private val voiceLogRepository: VoiceLogRepository,
    private val actionRepository: ActionRepository,
    private val ledScheduleRepository: LedScheduleRepository,
    private val productSettingRepository: ProductSettingRepository,
    private val slamTrackDataRepository: SlamTrackDataRepository
) {

    // ==================== Product ====================

    @GetMapping("/products")
    fun getAllProducts(): ResponseEntity<List<Map<String, Any?>>> {
        val products: List<Product> = productRepository.findAll()

        val result: List<Map<String, Any?>> = products.map { p ->
            mapOf<String, Any?>(
                "id" to p.id,
                "name" to p.name,
                "productNumber" to p.productNumber,
                "ipAddress" to p.ipAddress
            )
        }

        return ResponseEntity.ok(result)
    }

    @PostMapping("/product/ip")
    fun updateProductIp(
        @RequestParam productId: Long,
        @RequestParam ip: String
    ): ResponseEntity<String> {
        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        product.ipAddress = ip
        productRepository.save(product)
        return ResponseEntity.ok("IP updated successfully")
    }

    @PostMapping("/product/video-frame")
    fun uploadVideoFrame(
        @RequestParam productId: Long,
        @RequestParam frame: MultipartFile
    ): ResponseEntity<String> {
        println("[VIDEO] Received frame from product $productId, size: ${frame.size} bytes")
        return ResponseEntity.ok("Frame received")
    }

    @PostMapping("/product/slam-frame")
    fun uploadSlamFrame(
        @RequestParam productId: Long,
        @RequestParam frame: MultipartFile
    ): ResponseEntity<String> {
        println("[SLAM] Received frame from product $productId, size: ${frame.size} bytes")
        return ResponseEntity.ok("SLAM frame received")
    }

    // ==================== Motor Mode ====================

    @GetMapping("/motor-mode")
    fun getMotorMode(@RequestParam productId: Long): ResponseEntity<Map<String, Any?>> {
        val motorMode = motorModeRepository.findByProductId(productId)
            ?: return ResponseEntity.ok(mapOf("mode" to "STOP", "updatedAt" to null))

        return ResponseEntity.ok(mapOf(
            "mode" to motorMode.mode,
            "updatedAt" to motorMode.updatedAt
        ))
    }

    @PostMapping("/motor-mode")
    fun setMotorMode(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val productId = (request["productId"] as Number).toLong()
        val modeStr = request["mode"] as String

        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        val motorMode = motorModeRepository.findByProductId(productId) ?: MotorMode().apply {
            this.product = product
        }

        motorMode.mode = modeStr.uppercase()
        motorMode.updatedAt = LocalDateTime.now()
        motorModeRepository.save(motorMode)

        return ResponseEntity.ok("Motor mode set to $modeStr")
    }

    // ==================== Motor Control ====================

    @GetMapping("/motor-control/pending")
    fun getPendingMotorCommands(@RequestParam productId: Long): ResponseEntity<List<Map<String, Any>>> {
        val commands: List<MotorControl> = motorControlRepository.findByProductIdAndExecutedFalseOrderByCreatedAtDesc(productId)

        val result: List<Map<String, Any>> = commands.map { cmd ->
            mapOf<String, Any>(
                "id" to cmd.id,
                "commandType" to cmd.commandType,
                "createdAt" to cmd.createdAt
            )
        }

        return ResponseEntity.ok(result)
    }

    @PostMapping("/motor-control")
    fun sendMotorCommand(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val productId = (request["productId"] as Number).toLong()
        val commandTypeStr = request["commandType"] as String

        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        val motorControl = MotorControl().apply {
            this.product = product
            this.commandType = commandTypeStr.uppercase()
        }

        motorControlRepository.save(motorControl)
        return ResponseEntity.ok("Motor command sent")
    }

    @PostMapping("/motor-control/{id}/execute")
    fun markMotorCommandExecuted(@PathVariable id: Long): ResponseEntity<String> {
        val command = motorControlRepository.findById(id).orElse(null)
            ?: return ResponseEntity.badRequest().body("Command not found")

        command.executed = true
        command.executedAt = LocalDateTime.now()
        motorControlRepository.save(command)

        return ResponseEntity.ok("Command marked as executed")
    }

    // ==================== Motor Data ====================

    @PostMapping("/motor-data")
    fun saveMotorData(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val productId = (request["productId"] as Number).toLong()
        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        val motorData = MotorData().apply {
            this.product = product
            this.mode = request["mode"]?.let { MotorData.MotorMode.valueOf(it.toString().uppercase()) }
            this.direction = request["direction"]?.let { MotorData.Direction.valueOf(it.toString().uppercase()) }
            this.leftSpeed = (request["leftSpeed"] as? Number)?.toInt()
            this.rightSpeed = (request["rightSpeed"] as? Number)?.toInt()
            this.ultrasonicCm = (request["ultrasonicCm"] as? Number)?.toFloat()
            this.irDetected = request["irDetected"] as? Boolean
            this.source = request["source"]?.let { MotorData.Source.valueOf(it.toString().uppercase()) }
        }

        motorDataRepository.save(motorData)
        return ResponseEntity.ok("Motor data saved")
    }

    // ==================== Light Mode ====================

    @GetMapping("/light-mode")
    fun getLightMode(@RequestParam productId: Long): ResponseEntity<Map<String, Any?>> {
        return try {
            // 제품이 존재하는지 먼저 확인
            if (!productRepository.existsById(productId)) {
                return ResponseEntity.badRequest().body(mapOf("error" to "Product not found"))
            }

            val lightMode = lightModeRepository.findByProductId(productId)
                ?: return ResponseEntity.ok(mapOf(
                    "mode" to "AUTO",
                    "updatedAt" to null
                ))

            ResponseEntity.ok(mapOf(
                "mode" to lightMode.mode.name,
                "updatedAt" to lightMode.updatedAt.toString()
            ))
        } catch (e: Exception) {
            println("Error in getLightMode: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(500).body(mapOf("error" to "Internal server error: ${e.message}"))
        }
    }

    @PostMapping("/light-mode")
    fun setLightMode(@RequestBody request: Map<String, Any>): ResponseEntity<Any> {
        return try {
            // 입력 검증
            val productId = (request["productId"] as? Number)?.toLong()
                ?: return ResponseEntity.badRequest().body(mapOf("error" to "productId is required"))

            val modeStr = (request["mode"] as? String)
                ?: return ResponseEntity.badRequest().body(mapOf("error" to "mode is required"))

            // 제품 확인
            val product = productRepository.findById(productId).orElse(null)
                ?: return ResponseEntity.badRequest().body(mapOf("error" to "Product not found"))

            // 모드 검증
            val validModes = listOf("AUTO", "MANUAL", "APP")
            if (!validModes.contains(modeStr.uppercase())) {
                return ResponseEntity.badRequest().body(mapOf(
                    "error" to "Invalid mode. Valid modes are: ${validModes.joinToString(", ")}"
                ))
            }

            // LightMode 찾거나 생성
            val lightMode = lightModeRepository.findByProductId(productId) ?: LightMode().apply {
                this.product = product
            }

            // 모드 설정
            lightMode.mode = LightMode.Mode.valueOf(modeStr.uppercase())
            lightMode.updatedAt = LocalDateTime.now()

            // 저장
            val saved = lightModeRepository.save(lightMode)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Light mode set to $modeStr",
                "mode" to saved.mode.name,
                "updatedAt" to saved.updatedAt.toString()
            ))
        } catch (e: IllegalArgumentException) {
            println("Invalid mode in setLightMode: ${e.message}")
            ResponseEntity.badRequest().body(mapOf("error" to "Invalid mode: ${e.message}"))
        } catch (e: Exception) {
            println("Error in setLightMode: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(500).body(mapOf("error" to "Internal server error: ${e.message}"))
        }
    }

    // ==================== Light Control ====================

    @GetMapping("/light-control/pending")
    fun getPendingLightCommands(@RequestParam productId: Long): ResponseEntity<List<Map<String, Any>>> {
        val commands: List<LightControl> = lightControlRepository.findByProductIdAndExecutedFalseOrderByCreatedAtDesc(productId)

        val result: List<Map<String, Any>> = commands.map { cmd ->
            mapOf<String, Any>(
                "id" to cmd.id,
                "r" to cmd.ledColorR,
                "g" to cmd.ledColorG,
                "b" to cmd.ledColorB,
                "createdAt" to cmd.createdAt
            )
        }

        return ResponseEntity.ok(result)
    }

    @PostMapping("/light-control")
    fun sendLightCommand(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val productId = (request["productId"] as Number).toLong()
        val r = (request["red"] as? Number ?: request["r"] as Number).toInt()
        val g = (request["green"] as? Number ?: request["g"] as Number).toInt()
        val b = (request["blue"] as? Number ?: request["b"] as Number).toInt()

        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        val lightControl = LightControl().apply {
            this.product = product
            this.ledColorR = r
            this.ledColorG = g
            this.ledColorB = b
        }

        lightControlRepository.save(lightControl)
        return ResponseEntity.ok("Light command sent")
    }

    @PostMapping("/light-control/{id}/execute")
    fun markLightCommandExecuted(@PathVariable id: Long): ResponseEntity<String> {
        val command = lightControlRepository.findById(id).orElse(null)
            ?: return ResponseEntity.badRequest().body("Command not found")

        command.executed = true
        command.executedAt = LocalDateTime.now()
        lightControlRepository.save(command)

        return ResponseEntity.ok("Command marked as executed")
    }

    // ==================== Voice Command ====================

    @GetMapping("/voice-command")
    fun getVoiceCommands(@RequestParam productId: Long): ResponseEntity<List<Map<String, Any>>> {
        val commands: List<VoiceCommand> = voiceCommandRepository.findByProductId(productId)

        val result: List<Map<String, Any>> = commands.map { cmd ->
            mapOf<String, Any>(
                "id" to cmd.id,
                "inputText" to cmd.inputText,
                "actionName" to cmd.action.name,
                "actionCode" to cmd.action.code
            )
        }

        return ResponseEntity.ok(result)
    }

    @PostMapping("/voice-command")
    fun addVoiceCommand(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val productId = (request["productId"] as Number).toLong()
        val inputText = request["inputText"] as String
        val actionId = (request["actionId"] as Number).toLong()

        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        val action = actionRepository.findById(actionId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Action not found")

        val voiceCommand = VoiceCommand().apply {
            this.product = product
            this.inputText = inputText
            this.action = action
        }

        voiceCommandRepository.save(voiceCommand)
        return ResponseEntity.ok("Voice command mapping added")
    }

    @DeleteMapping("/voice-command/{id}")
    fun deleteVoiceCommand(@PathVariable id: Long): ResponseEntity<String> {
        if (!voiceCommandRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Voice command not found")
        }
        voiceCommandRepository.deleteById(id)
        return ResponseEntity.ok("Voice command deleted")
    }

    // ==================== Action ====================

    @GetMapping("/actions")
    fun getAllActions(): ResponseEntity<List<Map<String, Any>>> {
        val actions: List<Action> = actionRepository.findAll()

        val result: List<Map<String, Any>> = actions.map { a ->
            mapOf<String, Any>(
                "id" to a.id,
                "code" to a.code,
                "name" to a.name,
                "description" to (a.description ?: "")
            )
        }

        return ResponseEntity.ok(result)
    }

    // ==================== Voice Log ====================

    @PostMapping("/voice-log")
    fun saveVoiceLog(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val productId = (request["productId"] as Number).toLong()
        val inputText = request["inputText"] as String
        val matchedActionId = (request["matchedActionId"] as? Number)?.toLong()

        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        val voiceLog = VoiceLog().apply {
            this.product = product
            this.inputText = inputText
            matchedActionId?.let { actionId ->
                this.matchedAction = actionRepository.findById(actionId).orElse(null)
            }
        }

        voiceLogRepository.save(voiceLog)
        return ResponseEntity.ok("Voice log saved")
    }

    @GetMapping("/voice-log")
    fun getVoiceLogs(@RequestParam productId: Long): ResponseEntity<List<Map<String, Any?>>> {
        val logs: List<VoiceLog> = voiceLogRepository.findTop50ByProductIdOrderByCreatedAtDesc(productId)

        val result: List<Map<String, Any?>> = logs.map { log ->
            mapOf<String, Any?>(
                "id" to log.id,
                "inputText" to log.inputText,
                "matchedActionName" to log.matchedAction?.name,
                "createdAt" to log.createdAt
            )
        }

        return ResponseEntity.ok(result)
    }

    // ==================== LED Schedule ====================

    @GetMapping("/led/schedule")
    fun getLedSchedules(@RequestParam productId: Long): ResponseEntity<List<Map<String, Any>>> {
        val schedules: List<LedSchedule> = ledScheduleRepository.findByProductIdAndExecutedFalseOrderByScheduledTimeAsc(productId)

        val result: List<Map<String, Any>> = schedules.map { schedule ->
            mapOf<String, Any>(
                "id" to schedule.id,
                "scheduledTime" to schedule.scheduledTime,
                "ledColorR" to schedule.ledColorR,
                "ledColorG" to schedule.ledColorG,
                "ledColorB" to schedule.ledColorB,
                "executed" to schedule.executed
            )
        }

        return ResponseEntity.ok(result)
    }

    @PostMapping("/led/schedule")
    fun addLedSchedule(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val productId = (request["productId"] as Number).toLong()
        val scheduledTime = LocalDateTime.parse(request["scheduledTime"] as String)
        val r = (request["ledColorR"] as Number).toInt()
        val g = (request["ledColorG"] as Number).toInt()
        val b = (request["ledColorB"] as Number).toInt()

        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        val schedule = LedSchedule().apply {
            this.product = product
            this.scheduledTime = scheduledTime
            this.ledColorR = r
            this.ledColorG = g
            this.ledColorB = b
        }

        ledScheduleRepository.save(schedule)
        return ResponseEntity.ok("LED schedule added")
    }

    @DeleteMapping("/led/schedule/{id}")
    fun deleteLedSchedule(@PathVariable id: Long): ResponseEntity<String> {
        if (!ledScheduleRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Schedule not found")
        }
        ledScheduleRepository.deleteById(id)
        return ResponseEntity.ok("LED schedule deleted")
    }

    @PostMapping("/led/schedule/mark-executed")
    fun markLedScheduleExecuted(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val scheduleId = (request["id"] as Number).toLong()

        val schedule = ledScheduleRepository.findById(scheduleId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Schedule not found")

        schedule.executed = true
        ledScheduleRepository.save(schedule)

        return ResponseEntity.ok("Schedule marked as executed")
    }

    // ==================== Product Settings ====================

    @GetMapping("/product-settings")
    fun getProductSettings(@RequestParam productId: Long): ResponseEntity<List<Map<String, String>>> {
        val settings: List<ProductSettings> = productSettingRepository.findByProductId(productId)

        val result: List<Map<String, String>> = settings.map { setting ->
            mapOf<String, String>(
                "settingKey" to setting.settingKey,
                "settingValue" to setting.settingValue
            )
        }

        return ResponseEntity.ok(result)
    }

    // ==================== SLAM Track Data ====================

    @PostMapping("/slam/track-data")
    fun saveSlamTrackData(@RequestBody request: Map<String, Any>): ResponseEntity<String> {
        val productId = (request["productId"] as Number).toLong()
        val posX = (request["posX"] as Number).toFloat()
        val posY = (request["posY"] as Number).toFloat()
        val posZ = (request["posZ"] as? Number)?.toFloat() ?: 0f

        val product = productRepository.findById(productId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Product not found")

        val slamData = SlamTrackData().apply {
            this.product = product
            this.posX = posX
            this.posY = posY
            this.posZ = posZ
        }

        slamTrackDataRepository.save(slamData)
        return ResponseEntity.ok("SLAM track data saved")
    }

    @GetMapping("/slam/track-data")
    fun getSlamTrackData(@RequestParam productId: Long): ResponseEntity<List<Map<String, Any>>> {
        val data: List<SlamTrackData> = slamTrackDataRepository.findTop100ByProductIdOrderByRecordedAtDesc(productId)

        val result: List<Map<String, Any>> = data.map { d ->
            mapOf<String, Any>(
                "id" to d.id,
                "posX" to d.posX,
                "posY" to d.posY,
                "posZ" to d.posZ,
                "recordedAt" to d.recordedAt
            )
        }

        return ResponseEntity.ok(result)
    }
}