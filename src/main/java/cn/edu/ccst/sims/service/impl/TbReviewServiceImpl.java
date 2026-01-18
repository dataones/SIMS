package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.*;
import cn.edu.ccst.sims.mapper.*;
import cn.edu.ccst.sims.service.TbReviewService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
                        .eq(TbOrder::getStatus, 1));

        if (order == null) {
            return Result.error("订单不存在或未支付");
        }

        // 防止重复评价
        Long count = reviewMapper.selectCount(
                new LambdaQueryWrapper<TbReview>()
                        .eq(TbReview::getUserId, userId)
                        .eq(TbReview::getVenueId, order.getRelatedId()));
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
                        .eq(TbOrder::getOrderNo, orderNo));

        if (order == null) {
            return Result.error("订单不存在");
        }

        List<String> contents = reviewMapper.selectList(
                new LambdaQueryWrapper<TbReview>()
                        .eq(TbReview::getVenueId, order.getRelatedId())
                        .eq(TbReview::getAuditStatus, 1))
                .stream().map(TbReview::getContent).toList();

        return Result.success(contents);
    }

    @Override
    public Result<Map<String, Object>> getReviewsByVenueId(Long venueId) {

        List<TbReview> reviews = this.list(
                new LambdaQueryWrapper<TbReview>()
                        .eq(TbReview::getVenueId, venueId)
                        .eq(TbReview::getAuditStatus, 1)
                        .orderByDesc(TbReview::getCreateTime));

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
                        .orderByDesc(TbReview::getCreateTime));

        Map<Long, SysUser> userMap = userMapper.selectBatchIds(
                reviews.stream().map(TbReview::getUserId).distinct().toList()).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        Map<Long, TbVenue> venueMap = venueMapper.selectBatchIds(
                reviews.stream().map(TbReview::getVenueId).distinct().toList()).stream()
                .collect(Collectors.toMap(TbVenue::getId, v -> v));

        List<Map<String, Object>> list = new ArrayList<>();

        for (TbReview r : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("content", r.getContent());
            map.put("rating", r.getRating());
            map.put("auditStatus", r.getAuditStatus());
            map.put("createTime", r.getCreateTime());

            SysUser u = userMap.get(r.getUserId());
            if (u != null)
                map.put("nickname", u.getNickname());

            TbVenue v = venueMap.get(r.getVenueId());
            if (v != null)
                map.put("venueName", v.getName());

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
                        .eq(TbReview::getAuditStatus, 1));

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

    // 新增方法实现
    @Override
    public void createReview(Long userId, cn.edu.ccst.sims.dto.ReviewDTO reviewDTO) {
        // TODO: 实现创建评价逻辑
        System.out.println("用户 " + userId + " 创建评价: " + reviewDTO.getContent());
    }

    @Override
    public Map<String, Object> getVenueReviews(Long venueId, Integer pageNum, Integer pageSize, String sortBy) {
        // 参数校验
        if (venueId == null) {
            throw new IllegalArgumentException("场馆ID不能为空");
        }
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        // 构建查询条件
        LambdaQueryWrapper<TbReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TbReview::getVenueId, venueId)
                .eq(TbReview::getAuditStatus, 1); // 只显示已通过审核的评价

        // 排序逻辑
        if ("highest".equals(sortBy)) {
            queryWrapper.orderByDesc(TbReview::getRating)
                    .orderByDesc(TbReview::getCreateTime);
        } else if ("lowest".equals(sortBy)) {
            queryWrapper.orderByAsc(TbReview::getRating)
                    .orderByDesc(TbReview::getCreateTime);
        } else {
            // 默认按时间排序
            queryWrapper.orderByDesc(TbReview::getCreateTime);
        }

        // 分页查询
        Page<TbReview> page = new Page<>(pageNum, pageSize);
        Page<TbReview> reviewPage = this.page(page, queryWrapper);

        // 构建返回数据
        List<Map<String, Object>> reviewList = new ArrayList<>();
        for (TbReview review : reviewPage.getRecords()) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("venueId", review.getVenueId());
            reviewMap.put("userId", review.getUserId());
            reviewMap.put("rating", review.getRating());
            reviewMap.put("content", review.getContent());
            reviewMap.put("auditStatus", review.getAuditStatus());
            reviewMap.put("createTime", review.getCreateTime());

            // 获取用户信息
            SysUser user = userMapper.selectById(review.getUserId());
            if (user != null) {
                reviewMap.put("nickname", user.getNickname());
                reviewMap.put("avatar", user.getAvatar());
            } else {
                reviewMap.put("nickname", "匿名用户");
                reviewMap.put("avatar", null);
            }

            reviewList.add(reviewMap);
        }

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", reviewList);
        result.put("total", reviewPage.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    @Override
    public Map<String, Object> getUserReviews(Long userId, Integer pageNum, Integer pageSize) {
        // 参数校验
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        // 构建查询条件
        LambdaQueryWrapper<TbReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TbReview::getUserId, userId)
                .orderByDesc(TbReview::getCreateTime);

        // 分页查询
        Page<TbReview> page = new Page<>(pageNum, pageSize);
        Page<TbReview> reviewPage = this.page(page, queryWrapper);

        // 构建返回数据
        List<Map<String, Object>> reviewList = new ArrayList<>();
        for (TbReview review : reviewPage.getRecords()) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("venueId", review.getVenueId());
            reviewMap.put("userId", review.getUserId());
            reviewMap.put("rating", review.getRating());
            reviewMap.put("content", review.getContent());
            reviewMap.put("auditStatus", review.getAuditStatus());
            reviewMap.put("createTime", review.getCreateTime());

            // 获取场馆信息
            TbVenue venue = venueMapper.selectById(review.getVenueId());
            if (venue != null) {
                reviewMap.put("venueName", venue.getName());
            } else {
                reviewMap.put("venueName", "未知场馆");
            }

            reviewList.add(reviewMap);
        }

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", reviewList);
        result.put("total", reviewPage.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    @Override
    public Map<String, Object> getAllReviews(Integer pageNum, Integer pageSize, Integer auditStatus, String venueName) {
        // 参数校验
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        // 构建查询条件
        LambdaQueryWrapper<TbReview> queryWrapper = new LambdaQueryWrapper<>();

        // 审核状态筛选
        if (auditStatus != null) {
            queryWrapper.eq(TbReview::getAuditStatus, auditStatus);
        }

        // 场馆名称筛选 - 需要关联查询
        if (venueName != null && !venueName.trim().isEmpty()) {
            // 先查询匹配的场馆ID
            LambdaQueryWrapper<TbVenue> venueQuery = new LambdaQueryWrapper<>();
            venueQuery.like(TbVenue::getName, venueName);
            List<TbVenue> venues = venueMapper.selectList(venueQuery);

            if (!venues.isEmpty()) {
                List<Long> venueIds = venues.stream().map(TbVenue::getId).collect(Collectors.toList());
                queryWrapper.in(TbReview::getVenueId, venueIds);
            } else {
                // 如果没有匹配的场馆，返回空结果
                Map<String, Object> result = new HashMap<>();
                result.put("records", new ArrayList<>());
                result.put("total", 0L);
                result.put("pageNum", pageNum);
                result.put("pageSize", pageSize);
                return result;
            }
        }

        // 按时间倒序排列
        queryWrapper.orderByDesc(TbReview::getCreateTime);

        // 分页查询
        Page<TbReview> page = new Page<>(pageNum, pageSize);
        Page<TbReview> reviewPage = this.page(page, queryWrapper);

        // 批量获取用户和场馆信息
        List<Long> userIds = reviewPage.getRecords().stream()
                .map(TbReview::getUserId).distinct().collect(Collectors.toList());
        List<Long> venueIds = reviewPage.getRecords().stream()
                .map(TbReview::getVenueId).distinct().collect(Collectors.toList());

        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        Map<Long, TbVenue> venueMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<TbVenue> venues = venueMapper.selectBatchIds(venueIds);
            venueMap = venues.stream().collect(Collectors.toMap(TbVenue::getId, v -> v));
        }

        // 构建返回数据
        List<Map<String, Object>> reviewList = new ArrayList<>();
        for (TbReview review : reviewPage.getRecords()) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("venueId", review.getVenueId());
            reviewMap.put("userId", review.getUserId());
            reviewMap.put("rating", review.getRating());
            reviewMap.put("content", review.getContent());
            reviewMap.put("auditStatus", review.getAuditStatus());
            reviewMap.put("createTime", review.getCreateTime());

            // 获取用户信息
            SysUser user = userMap.get(review.getUserId());
            if (user != null) {
                reviewMap.put("nickname", user.getNickname());
            } else {
                reviewMap.put("nickname", "匿名用户");
            }

            // 获取场馆信息
            TbVenue venue = venueMap.get(review.getVenueId());
            if (venue != null) {
                reviewMap.put("venueName", venue.getName());
            } else {
                reviewMap.put("venueName", "未知场馆");
            }

            reviewList.add(reviewMap);
        }

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", reviewList);
        result.put("total", reviewPage.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    @Override
    public boolean auditReview(Long reviewId, Integer auditStatus, Long adminId) {
        // 参数校验
        if (reviewId == null) {
            throw new IllegalArgumentException("评价ID不能为空");
        }
        if (auditStatus == null || (auditStatus != 0 && auditStatus != 1 && auditStatus != 2)) {
            throw new IllegalArgumentException("审核状态必须是0(待审核)、1(已通过)或2(已隐藏)");
        }
        if (adminId == null) {
            throw new IllegalArgumentException("管理员ID不能为空");
        }

        // 验证管理员权限
        SysUser admin = userMapper.selectById(adminId);
        if (admin == null || admin.getRole() != 2) {
            throw new IllegalArgumentException("无管理员权限");
        }

        // 查询评价是否存在
        TbReview review = this.getById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }

        // 更新审核状态
        review.setAuditStatus(auditStatus);
        boolean result = this.updateById(review);

        if (result) {
            log.info("管理员 {} 审核评价 {}，状态设置为 {}", adminId, reviewId, auditStatus);
        }

        return result;
    }

    @Override
    public boolean deleteReview(Long reviewId, Long userId) {
        // 参数校验
        if (reviewId == null) {
            throw new IllegalArgumentException("评价ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 查询评价是否存在
        TbReview review = this.getById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }

        // 验证权限：只有评价作者或管理员可以删除
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 检查是否是评价作者或管理员
        if (!review.getUserId().equals(userId) && user.getRole() != 2) {
            throw new IllegalArgumentException("无权限删除此评价");
        }

        // 删除评价
        boolean result = this.removeById(reviewId);

        if (result) {
            log.info("用户 {} 删除评价 {}", userId, reviewId);
        }

        return result;
    }
}
