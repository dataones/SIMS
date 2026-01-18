package cn.edu.ccst.sims.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单结算DTO
 */
@Data
@Schema(description = "订单结算信息")
public class OrderSettlementDTO {

    @NotNull(message = "场馆ID不能为空")
    @Schema(description = "场馆ID", example = "1")
    private Long venueId;

    @NotNull(message = "预订日期不能为空")
    @Schema(description = "预订日期", example = "2024-01-15")
    private String bookingDate;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", example = "09:00")
    private String startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", example = "11:00")
    private String endTime;

    @NotNull(message = "数量不能为空")
    @Schema(description = "预订数量", example = "1")
    private Integer quantity;

    @Schema(description = "基础费用", example = "200.00")
    private BigDecimal baseFee;

    @Schema(description = "附加服务列表")
    private List<AdditionalService> additionalServices;

    @Schema(description = "优惠码", example = "DISCOUNT10")
    private String discountCode;

    @Schema(description = "用户ID", example = "123")
    private Long userId;

    @Schema(description = "订单类型", example = "VENUE")
    private String orderType; // VENUE, EQUIPMENT

    @Data
    @Schema(description = "附加服务")
    public static class AdditionalService {
        @Schema(description = "服务ID", example = "1")
        private Long serviceId;

        @Schema(description = "服务名称", example = "篮球")
        private String serviceName;

        @Schema(description = "服务价格", example = "50.00")
        private BigDecimal price;

        @Schema(description = "服务数量", example = "2")
        private Integer quantity;
    }
}
