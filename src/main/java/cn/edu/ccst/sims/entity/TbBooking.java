package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 场馆预约表，对应场馆预订、预定管理
 */
@Data
@TableName("tb_booking")
public class TbBooking {
    /**
     * 预约ID
     */
    @TableId
    private Long id;
    /**
     * 预约单号(关联订单)
     */
    private String orderNo;
    /**
     * 预约人ID
     */
    private Long userId;
    /**
     * 场馆ID
     */
    private Long venueId;
    /**
     * 预约日期
     */
    private LocalDate date;
    /**
     * 开始时间(如 14:00)
     */
    private String startTime;
    /**
     * 结束时间(如 15:00)
     */
    private String endTime;
    /**
     * 预估费用
     */
    private BigDecimal totalPrice;
    /**
     * 状态: 0-待审核, 1-已通过, 2-已驳回, 3-已取消, 4-已完成
     */
    private Integer status;
    /**
     * 提交时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}