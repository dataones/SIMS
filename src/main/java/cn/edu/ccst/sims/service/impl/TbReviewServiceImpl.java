package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbOrder;
import cn.edu.ccst.sims.entity.TbReview;
import cn.edu.ccst.sims.mapper.TbOrderMapper;
import cn.edu.ccst.sims.mapper.TbReviewMapper;
import cn.edu.ccst.sims.service.TbReviewService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TbReviewServiceImpl implements TbReviewService {

    private final TbReviewMapper reviewMapper;
    private final TbOrderMapper orderMapper;

    public TbReviewServiceImpl(TbReviewMapper reviewMapper, TbOrderMapper orderMapper) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public Result<Void> submitReview(Long userId, String orderNo, String content, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            return Result.error("评分必须在1~5之间");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评价内容不能为空");
        }

        // 1. 查询订单
        LambdaQueryWrapper<TbOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(TbOrder::getOrderNo, orderNo)
                .eq(TbOrder::getUserId, userId)
                .eq(TbOrder::getStatus, 1); // 已支付
        TbOrder order = orderMapper.selectOne(orderQuery);

        if (order == null) {
            return Result.error("订单不存在或未完成支付");
        }

        // 2. 保存评价
        TbReview review = new TbReview();
        review.setUserId(userId);
        review.setVenueId(order.getRelatedId()); // 关联的场馆或器材ID
        review.setContent(content);
        review.setRating(rating);
        review.setAuditStatus(1); // 默认已通过审核
        reviewMapper.insert(review);

        return Result.success();
    }

    @Override
    public Result<List<String>> getReviewsByOrderNo(String orderNo) {
        LambdaQueryWrapper<TbOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(TbOrder::getOrderNo, orderNo);
        TbOrder order = orderMapper.selectOne(orderQuery);
        if (order == null) {
            return Result.error("订单不存在");
        }

        LambdaQueryWrapper<TbReview> reviewQuery = new LambdaQueryWrapper<>();
        reviewQuery.eq(TbReview::getVenueId, order.getRelatedId())
                .eq(TbReview::getAuditStatus, 1);
        var reviews = reviewMapper.selectList(reviewQuery);
        var reviewContents = reviews.stream().map(TbReview::getContent).toList();

        return Result.success(reviewContents);
    }
}
