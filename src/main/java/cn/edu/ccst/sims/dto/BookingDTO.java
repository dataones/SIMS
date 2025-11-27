package cn.edu.ccst.sims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 场馆预约数据传输对象
 */
@Data
public class BookingDTO {

    /**
     * 预约ID（修改时需要）
     */
    private Long id;

    /**
     * 场馆ID
     */
    @NotNull(message = "场馆ID不能为空")
    private Long venueId;

    /**
     * 预约日期
     */
    @NotNull(message = "预约日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确，请使用HH:mm格式")
    private String startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确，请使用HH:mm格式")
    private String endTime;

    /**
     * 预估费用
     */
    private BigDecimal totalPrice;

    /**
     * 备注信息
     */
    private String remark;
}