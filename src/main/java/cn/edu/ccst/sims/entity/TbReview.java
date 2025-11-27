package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价信息表，对应用户评价、评价审核
 */
@Data
@TableName("tb_review")
public class TbReview {
    /**
     * 评价ID
     */
    @TableId
    private Long id;
    /**
     * 评价人ID
     */
    private Long userId;
    /**
     * 评价场馆ID
     */
    private Long venueId;
    /**
     * 评价内容
     */
    private String content;
    /**
     * 评分1-5星
     */
    private Integer rating;
    /**
     * 状态: 0-待审核, 1-已通过, 2-违规隐藏
     */
    private Integer auditStatus;
    /**
     * 评价时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}