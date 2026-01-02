package cn.edu.ccst.sims.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class VenueVO {
    private Long id;
    private String name;
    private String type;         // 分类
    private BigDecimal price;    // 价格
    private String location;     // 位置
    private String description;  // 简介
    private String remark;       // 说明
    private String image;        // 图片
    private String openTime;     // 开放时间
    private String closeTime;    // 关闭时间
    private Integer status;      // 1-正常, 0-维护中
    private Date createTime;
    private Date updateTime;
}