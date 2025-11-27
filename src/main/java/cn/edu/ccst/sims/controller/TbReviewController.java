package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.ReviewDTO;
import cn.edu.ccst.sims.service.TbReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TbReviewController {

    private final TbReviewService reviewService;

    public TbReviewController(TbReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** 提交评价，基于订单号 */
    @PostMapping("/reviews")
    public Result<Void> submitReview(@AuthenticationPrincipal Long userId,
                                     @RequestParam String orderNo,  // 使用订单号而非场馆ID
                                     @RequestParam @NotBlank String content,
                                     @RequestParam @Min(1) @Max(5) Integer rating) {
        return reviewService.submitReview(userId, orderNo, content, rating);
    }

    /** 查询订单的评价列表 */
    @GetMapping("/reviews/{orderNo}")
    public Result<List<String>> getReviewsByOrderNo(@PathVariable String orderNo) {
        return reviewService.getReviewsByOrderNo(orderNo);
    }
}
