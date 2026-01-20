package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款申请表
 */
@Data
@TableName("tb_refund")
public class TbRefund {
    /**
     * 退款ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 审核人ID
     */
    private Long auditUserId;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;

    /**
     * 申请时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
