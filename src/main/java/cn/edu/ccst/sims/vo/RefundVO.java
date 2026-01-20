package cn.edu.ccst.sims.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款申请VO
 */
@Data
public class RefundVO {
    /**
     * 退款ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 订单类型: 1-场馆预约, 2-器材租赁
     */
    private Integer orderType;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 详细说明
     */
    private String remark;

    /**
     * 退款状态: 0-待审核, 1-已批准, 2-已驳回, 3-退款完成
     */
    private Integer status;

    /**
     * 申请时间
     */
    private LocalDateTime createTime;
}
