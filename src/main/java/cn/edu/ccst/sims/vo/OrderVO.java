package cn.edu.ccst.sims.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    // ===== 订单公共信息 =====
    private String orderNo;
    private Integer type;        // 1-场馆预约 2-器材租赁
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createTime;

    // ===== 场馆预约信息 =====
    private String venueName;
    private LocalDate date;
    private String startTime;
    private String endTime;

    // ===== 器材租赁信息 =====
    private String equipmentName;
    private Integer count;
}

