package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.*;
import cn.edu.ccst.sims.mapper.*;
import cn.edu.ccst.sims.service.TbReviewService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TbReviewServiceImpl
        extends ServiceImpl<TbReviewMapper, TbReview>
        implements TbReviewService {

    private static final Logger log = LoggerFactory.getLogger(TbReviewServiceImpl.class);

    private final TbReviewMapper reviewMapper;
    private final TbOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final TbVenueMapper venueMapper;

    public TbReviewServiceImpl(TbReviewMapper reviewMapper,
                               TbOrderMapper orderMapper,
                               SysUserMapper userMapper,
                               TbVenueMapper venueMapper) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.venueMapper = venueMapper;
    }

    @Override
    public Result<Void> submitReview(Long userId, String orderNo, String content, Integer rating) {

        if (rating == null || rating < 1 || rating > 5) {
            return Result.error("评分必须在1~5之间");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评价内容不能为空");
        }

        // 查询订单
        TbOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getOrderNo, orderNo)
                        .eq(TbOrder::getUserId, userId)
                        .eq(TbOrder::getStatus, 1)
        );

        if (order == null) {
            return Result.error("订单不存在或未支付");
        }

        // 防止重复评价
        Long count = reviewMapper.selectCount(
                new LambdaQueryWrapper<TbReview>()
                        .eq(TbReview::getUserId, userId)
                        .eq(TbReview::getVenueId, order.getRelatedId())
        );
        if (count > 0) {
            return Result.error("该订单已评价");
        }

        TbReview review = new TbReview();
        review.setUserId(userId);
        review.setVenueId(order.getRelatedId());
        review.setContent(content);
        review.setRating(rating);
        review.setAuditStatus(1);
        review.setCreateTime(LocalDateTime.now());

        reviewMapper.insert(review);
        return Result.success();
    }

    @Override
    public Result<List<String>> getReviewsByOrderNo(String orderNo) {

        TbOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getOrderNo, orderNo)
        );

        if (order == null) {
            return Result.error("订单不存在");
        }

        List<String> contents = reviewMapper.selectList(
                new LambdaQueryWrapper<TbReview>()
                        .eq(TbReview::getVenueId, order.getRelatedId())
                        .eq(TbReview::getAuditStatus, 1)
        ).stream().map(TbReview::getContent).toList();

        return Result.success(contents);
    }

    @Override
    public Result<Map<String, Object>> getReviewsByVenueId(Long venueId) {

        List<TbReview> reviews = this.list(
                new LambdaQueryWrapper<TbReview>()
                        .eq(TbReview::getVenueId, venueId)
                        .eq(TbReview::getAuditStatus, 1)
                        .orderByDesc(TbReview::getCreateTime)
        );

        List<Map<String, Object>> list = new ArrayList<>();

        for (TbReview review : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", review.getId());
            map.put("content", review.getContent());
            map.put("rating", review.getRating());
            map.put("createTime", review.getCreateTime());

            SysUser user = userMapper.selectById(review.getUserId());
            if (user != null) {
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatar());
            }
            list.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());

        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getAllReviews() {

        List<TbReview> reviews = this.list(
                new LambdaQueryWrapper<TbReview>()
                        .orderByDesc(TbReview::getCreateTime)
        );

        Map<Long, SysUser> userMap = userMapper.selectBatchIds(
                reviews.stream().map(TbReview::getUserId).distinct().toList()
        ).stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        Map<Long, TbVenue> venueMap = venueMapper.selectBatchIds(
                reviews.stream().map(TbReview::getVenueId).distinct().toList()
        ).stream().collect(Collectors.toMap(TbVenue::getId, v -> v));

        List<Map<String, Object>> list = new ArrayList<>();

        for (TbReview r : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("content", r.getContent());
            map.put("rating", r.getRating());
            map.put("auditStatus", r.getAuditStatus());
            map.put("createTime", r.getCreateTime());

            SysUser u = userMap.get(r.getUserId());
            if (u != null) map.put("nickname", u.getNickname());

            TbVenue v = venueMap.get(r.getVenueId());
            if (v != null) map.put("venueName", v.getName());

            list.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());

        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getVenueReviewStats(Long venueId) {

        List<TbReview> reviews = this.list(
                new LambdaQueryWrapper<TbReview>()
                        .eq(TbReview::getVenueId, venueId)
                        .eq(TbReview::getAuditStatus, 1)
        );

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReviews", reviews.size());

        if (!reviews.isEmpty()) {
            double avg = reviews.stream().mapToInt(TbReview::getRating).average().orElse(0);
            stats.put("avgRating", String.format("%.1f", avg));
        } else {
            stats.put("avgRating", "0.0");
        }

        return Result.success(stats);
    }
}
