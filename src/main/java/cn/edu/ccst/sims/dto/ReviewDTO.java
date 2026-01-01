package cn.edu.ccst.sims.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String nickname;      // 用户昵称
    private String avatar;        // 用户头像
    private Long venueId;
    private String venueName;     // 场馆名称
    private String venueType;     // 场馆类型
    private String content;       // 评价内容
    private Integer rating;       // 评分 1-5
    private Integer auditStatus;  // 审核状态
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 返回给前端的格式化时间
    private String createTimeStr;
}