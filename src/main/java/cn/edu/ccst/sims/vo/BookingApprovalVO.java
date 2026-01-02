package cn.edu.ccst.sims.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class BookingApprovalVO {
    private Long id;
    private String orderNo;
    private String username;     // 申请人用户名
    private String nickname;     // 申请人昵称
    private String venueName;    // 场馆名称
    private Date date;           // 预约日期
    private String startTime;
    private String endTime;
    private BigDecimal totalPrice;
    private Integer status;      // 0-待审核
    private Date createTime;
}