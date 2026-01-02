package cn.edu.ccst.sims.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminStatsVO {
    private Long totalUsers;        // 注册用户
    private Long totalVenues;       // 场馆总数
    private Long pendingApprovals;  // 待审批 (预约待审 + 借用申请)
    private BigDecimal totalRevenue;// 平台总流水
}