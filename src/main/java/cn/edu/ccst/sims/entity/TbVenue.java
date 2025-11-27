package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 场馆信息表，对应场馆展示与查询、场馆资源管理
 */
@Data
@TableName("tb_venue")
public class TbVenue {
    /**
     * 场馆ID
     */
    @TableId
    private Long id;
    /**
     * 场馆名称
     */
    private String name;
    /**
     * 分类(篮球/羽毛球/游泳)
     */
    private String type;
    /**
     * 每小时费用(元)
     */
    private BigDecimal price;
    /**
     * 位置信息
     */
    private String location;
    /**
     * 场馆简介
     */
    private String description;
    /**
     * 图片地址
     */
    private String image;
    /**
     * 开放时间
     */
    private String openTime;
    /**
     * 关闭时间
     */
    private String closeTime;
    /**
     * 状态: 1-正常, 0-维护中
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}