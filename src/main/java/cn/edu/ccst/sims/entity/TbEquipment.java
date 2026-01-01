package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 器材信息表，对应器材管理、器材借用展示
 */
@Data
@TableName("tb_equipment")
public class TbEquipment {
    /**
     * 器材ID
     */
    @TableId
    private Long id;
    /**
     * 器材名称
     */
    private String name;
    /**
     * 租用单价(元/个)
     */
    private BigDecimal price;
    /**
     * 总库存
     */
    private Integer totalStock;
    /**
     * 已租出数量
     */
    private Integer rentedStock;
    /**
     * 图片
     */
    private String image;
    /**
     * 录入时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 规格
     */
    private String specification;
    /**
     * 品牌
     */
    private String brand;
}