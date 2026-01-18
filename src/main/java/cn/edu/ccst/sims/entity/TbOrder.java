package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付表，对应费用结算、订单详情、订单管理
 */
@Data
@TableName("tb_order")
public class TbOrder {
    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 订单号(唯一)
     */
    private String orderNo;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 关联业务ID(预约ID或借用ID)
     */
    private Long relatedId;
    /**
     * 类型: 1-场馆预约, 2-器材租赁
     */
    private Integer type;
    /**
     * 订单金额
     */
    private BigDecimal amount;
    /**
     * 状态: 0-未支付, 1-已支付, 2-已退款, 3-已取消
     */
    private Integer status;
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    /**
     * 下单时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}