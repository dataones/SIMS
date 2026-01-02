package cn.edu.ccst.sims.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserStatsVO {
    private BigDecimal balance;     // 当前余额
    private Long bookings;          // 我的预订数量
    private Long rentals;           // 我的借用数量
    private BigDecimal totalExpense;// 累计消费
}