package kr.mjc.kdj.web.controller

import kr.mjc.kdj.web.repository.User
import kr.mjc.kdj.web.repository.VoiceCommandRepository
import kr.mjc.kdj.web.service.ProductService
import kr.mjc.kdj.web.service.StatisticsService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttribute
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class DashboardController(
    private val productService: ProductService,
    private val statisticsService: StatisticsService,
    private val voiceCommandRepository: VoiceCommandRepository
) {

    /**
     * 대시보드 (메인 페이지)
     */
    @GetMapping("/", "/dashboard")
    fun dashboard(@SessionAttribute(required = false) user: User?, model: Model): String {
        if (user == null) {
            return "redirect:/login"
        }

        val products = productService.getUserProducts(user.id)
        model.addAttribute("user", user)
        model.addAttribute("products", products)
        return "dashboard"
    }

    /**
     * 마이페이지
     */
    @GetMapping("/mypage")
    fun mypage(@SessionAttribute(required = false) user: User?, model: Model): String {
        if (user == null) {
            return "redirect:/login"
        }

        val products = productService.getUserProducts(user.id)
        model.addAttribute("user", user)
        model.addAttribute("products", products)
        return "mypage"
    }

    /**
     * 제품 상세 페이지 (통계 포함)
     */
    @GetMapping("/product/detail")
    fun productDetail(
        @RequestParam productId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @SessionAttribute(required = false) user: User?,
        model: Model
    ): String {
        if (user == null) {
            return "redirect:/login"
        }

        val product = productService.getProduct(productId)

        // 권한 체크
        if (product.user?.id != user.id) {
            return "redirect:/dashboard?error=unauthorized"
        }

        // 최근 데이터 조회
        val recentLightData = productService.getRecentLightData(productId)
        val recentMotorData = productService.getRecentMotorData(productId)

        // 경로 추적용 데이터 (최대 100개)
        val pathMotorData = productService.getPathMotorData(productId, 100)

        // 통계 데이터 조회
        val lightStats = statisticsService.getLightStats(productId)
        val motorStats = statisticsService.getMotorStats(productId)
        val voiceStats = statisticsService.getVoiceStats(productId)

        // 음성 명령 매핑 목록
        val voiceCommands = voiceCommandRepository.findByProductId(productId)

        model.addAttribute("product", product)
        model.addAttribute("lightData", recentLightData)
        model.addAttribute("motorData", recentMotorData)
        model.addAttribute("pathMotorData", pathMotorData)
        model.addAttribute("lightStats", lightStats)
        model.addAttribute("motorStats", motorStats)
        model.addAttribute("voiceStats", voiceStats)
        model.addAttribute("voiceCommands", voiceCommands)

        return "product/detail"
    }

    /**
     * 조명 데이터 목록
     */
    @GetMapping("/product/light-data")
    fun lightData(
        @RequestParam productId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "desc") sort: String,
        @SessionAttribute(required = false) user: User?,
        model: Model
    ): String {
        if (user == null) {
            return "redirect:/login"
        }

        val product = productService.getProduct(productId)

        if (product.user?.id != user.id) {
            return "redirect:/dashboard?error=unauthorized"
        }

        val lightData = productService.getLightData(productId, PageRequest.of(page, 10), sort)

        model.addAttribute("product", product)
        model.addAttribute("lightData", lightData)
        model.addAttribute("currentSort", sort)

        return "product/light-data"
    }

    /**
     * 모터 데이터 목록
     */
    @GetMapping("/product/motor-data")
    fun motorData(
        @RequestParam productId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "desc") sort: String,
        @SessionAttribute(required = false) user: User?,
        model: Model
    ): String {
        if (user == null) {
            return "redirect:/login"
        }

        val product = productService.getProduct(productId)

        if (product.user?.id != user.id) {
            return "redirect:/dashboard?error=unauthorized"
        }

        val motorData = productService.getMotorData(productId, PageRequest.of(page, 10), sort)

        model.addAttribute("product", product)
        model.addAttribute("motorData", motorData)
        model.addAttribute("currentSort", sort)

        return "product/motor-data"
    }

    /**
     * 제품 등록 처리
     */
    @PostMapping("/product/register")
    fun registerProduct(
        @RequestParam productNumber: String,
        @RequestParam(required = false) name: String?,
        @SessionAttribute(required = false) user: User?,
        redirectAttributes: RedirectAttributes
    ): String {
        if (user == null) {
            return "redirect:/login"
        }

        return try {
            productService.registerProduct(productNumber, name, user.id, null)
            redirectAttributes.addFlashAttribute("message", "제품이 등록되었습니다.")
            "redirect:/dashboard"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/dashboard"
        }
    }

    /**
     * 제품 제거 처리
     */
    @PostMapping("/product/unregister")
    fun unregisterProduct(
        @RequestParam productId: Long,
        @SessionAttribute(required = false) user: User?,
        redirectAttributes: RedirectAttributes
    ): String {
        if (user == null) {
            return "redirect:/login"
        }

        return try {
            productService.unregisterProduct(productId, user.id)
            redirectAttributes.addFlashAttribute("message", "제품이 제거되었습니다.")
            "redirect:/mypage"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/mypage"
        }
    }
}