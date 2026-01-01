package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.common.Result;

import java.util.List;
import java.util.Map;

public interface TbReviewService {

    Result<Void> submitReview(Long userId, String orderNo, String content, Integer rating);

    Result<List<String>> getReviewsByOrderNo(String orderNo);

    Result<Map<String, Object>> getReviewsByVenueId(Long venueId);

    Result<Map<String, Object>> getAllReviews();

    Result<Map<String, Object>> getVenueReviewStats(Long venueId);
}
