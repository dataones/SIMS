package cn.edu.ccst.sims.dto;

import lombok.Data;

import java.time.LocalDateTime;
//返回评价对象
@Data
public class ReviewDTO {
    private String username;  // 加密后的用户名
    private String content;   // 评价内容
    private Integer rating;   // 评分
    private LocalDateTime createTime; // 评价时间
}
