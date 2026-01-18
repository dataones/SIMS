package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.ReviewDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

public interface TbReviewService {

    // 原有方法
    Result<Void> submitReview(Long userId, String orderNo, String content, Integer rating);

    Result<List<String>> getReviewsByOrderNo(String orderNo);

    Result<Map<String, Object>> getReviewsByVenueId(Long venueId);

    Result<Map<String, Object>> getAllReviews();

    Result<Map<String, Object>> getVenueReviewStats(Long venueId);

    // 新增方法
    /**
     * 创建评价
     * 
     * @param userId    用户ID
     * @param reviewDTO 评价信息
     */
    void createReview(Long userId, ReviewDTO reviewDTO);

    /**
     * 获取场馆评价列表
     * 
     * @param venueId  场馆ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param sortBy   排序方式
     * @return 评价列表
     */
    Map<String, Object> getVenueReviews(Long venueId, Integer pageNum, Integer pageSize, String sortBy);

    /**
     * 获取用户评价列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    Map<String, Object> getUserReviews(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 管理员获取所有评价
     * 
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @param auditStatus 审核状态
     * @param venueName   场馆名称
     * @return 评价列表
     */
    Map<String, Object> getAllReviews(Integer pageNum, Integer pageSize, Integer auditStatus, String venueName);

    /**
     * 管理员审核评价
     * 
     * @param reviewId    评价ID
     * @param auditStatus 审核状态
     * @param adminId     管理员ID
     * @return 是否成功
     */
    boolean auditReview(Long reviewId, Integer auditStatus, Long adminId);

    /**
     * 删除评价
     * 
     * @param reviewId 评价ID
     * @param userId   用户ID
     * @return 是否成功
     */
    boolean deleteReview(Long reviewId, Long userId);
}
