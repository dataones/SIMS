package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.ReviewDTO;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.service.TbReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class TbReviewController {

    private final TbReviewService reviewService;

    public TbReviewController(TbReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public Result<Void> submitReview(@AuthenticationPrincipal Long user,
                                     @RequestParam String orderNo,
                                     @RequestParam String content,
                                     @RequestParam Integer rating) {
        return reviewService.submitReview(user, orderNo, content, rating);
    }

    @GetMapping("/reviews/{orderNo}")
    public Result<List<String>> getReviewsByOrderNo(@PathVariable String orderNo) {
        return reviewService.getReviewsByOrderNo(orderNo);
    }

    @GetMapping("/venues/{venueId}/reviews")
    public Result<Map<String, Object>> getVenueReviews(@PathVariable Long venueId) {
        System.out.println(reviewService.getReviewsByVenueId(venueId));

        return reviewService.getReviewsByVenueId(venueId);
    }

    @GetMapping("/reviews/all")
    public Result<Map<String, Object>> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/venues/{venueId}/reviews/stats")
    public Result<Map<String, Object>> getVenueReviewStats(@PathVariable Long venueId) {

        return reviewService.getVenueReviewStats(venueId);
    }
}
