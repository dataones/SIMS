package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图表，对应首页轮播图展示
 */
@Data
@TableName("tb_banner")
public class TbBanner {
    /**
     * 轮播图ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 轮播图标题
     */
    private String title;
    /**
     * 轮播图描述
     */
    private String description;
    /**
     * 图片URL
     */
    private String imageUrl;
    /**
     * 跳转链接
     */
    private String link;
    /**
     * 排序权重（数字越大越靠前）
     */
    private Integer sortOrder;
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
