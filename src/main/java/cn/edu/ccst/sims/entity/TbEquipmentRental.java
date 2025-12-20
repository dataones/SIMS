package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 器材借用记录表，对应器材借用、归还处理
 */
@Data
@TableName("tb_equipment_rental")
public class TbEquipmentRental {
    /**
     * 记录ID
     */
    @TableId
    private Long id;
    /**
     * 借用人ID
     */

    private Long userId;
    /**
     * 借用单号
     */
    private String orderNo;
    /**
     * 器材ID
     */
    private Long equipmentId;
    /**
     * 租用数量
     */
    private Integer count;
    /**
     * 总费用
     */
    private BigDecimal price;
    /**
     * 状态: 1-申请中, 2-使用中, 3-已归还, 4-已驳回
     */
    private Integer status;
    /**
     * 借用时间
     */
    private LocalDateTime createTime;
    /**
     * 归还时间
     */
    private LocalDateTime returnTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}