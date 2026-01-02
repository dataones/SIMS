package cn.edu.ccst.sims.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 场馆预约视图对象
 */
@Data
public class BookingVO {

    /**
     * 预约ID
     */
    private Long id;

    /**
     * 预约单号
     */
    private String orderNo;

    /**
     * 预约人ID
     */
    private Long userId;

    /**
     * 预约人姓名
     */
    private String userName;

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 场馆名称
     */
    private String venueName;

    /**
     * 场馆类型
     */
    private String venueType;

    /**
     * 预约日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
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
     * 状态描述
     */
    private String statusText;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 获取状态描述
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已驳回";
            case 3: return "已取消";
            case 4: return "已完成";
            default: return "未知";
        }
    }
}