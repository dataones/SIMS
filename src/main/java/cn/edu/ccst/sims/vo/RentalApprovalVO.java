package cn.edu.ccst.sims.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RentalApprovalVO {
    private Long id;
    private String orderNo;
    private String username;      // 申请人
    private String nickname;
    private String equipmentName; // 器材名称
    private Integer count;        // 借用数量
    private BigDecimal price;     // 总价
    private Integer status;       // 1-申请中
    private Date createTime;
}