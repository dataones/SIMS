package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.common.Result;

import java.util.List;

public interface TbReviewService {

    // 提交评价，基于订单号
    Result<Void> submitReview(Long userId, String orderNo, String content, Integer rating);

    // 查询订单的评价列表
    Result<List<String>> getReviewsByOrderNo(String orderNo);
}
