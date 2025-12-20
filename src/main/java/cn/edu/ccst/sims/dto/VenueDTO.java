package cn.edu.ccst.sims.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VenueDTO {
    private Long id;
    private String name;
    private String type;       // 分类
    private BigDecimal price;  // 每小时费用
    private String location;
    private String description;
    private String image;
    private String openTime;
    private String closeTime;
    private Integer status;
    private String remark;// 1-正常, 0-维护中
}