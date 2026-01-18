package cn.edu.ccst.sims.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class OrderVO {

    // ===== 订单公共信息 =====
    private String orderNo;
    private Integer type; // 1-场馆预约 2-器材租赁
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createTime;
    private String username; // 用户名（用于管理端显示）
    private Long id; // 订单ID

    // ===== 场馆预约信息 =====
    private String venueName;
    private String venueImage; // 场馆图片
    private String venueAddress; // 场馆地址
    private LocalDate date;
    private String startTime;
    private String endTime;

    // ===== 器材租赁信息 =====
    private String equipmentName;
    private Integer count;
}
