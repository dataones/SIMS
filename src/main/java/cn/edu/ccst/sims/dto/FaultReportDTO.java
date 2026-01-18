package cn.edu.ccst.sims.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 故障申报DTO
 */
@Data
@Schema(description = "故障申报信息")
public class FaultReportDTO {

    @NotNull(message = "场馆ID不能为空")
    @Schema(description = "场馆ID", example = "1")
    private Long venueId;

    @NotBlank(message = "故障标题不能为空")
    @Schema(description = "故障标题", example = "照明设备故障")
    private String title;

    @NotBlank(message = "故障详情不能为空")
    @Schema(description = "故障详情", example = "场地灯光不亮，影响夜间使用")
    private String content;

    @Schema(description = "故障位置", example = "篮球场A区")
    private String location;

    @Schema(description = "紧急程度", example = "HIGH")
    private String urgency; // LOW, MEDIUM, HIGH

    @Schema(description = "联系方式", example = "13800138000")
    private String contactPhone;

    @Schema(description = "故障图片URL列表")
    private String[] images;

    @Schema(description = "期望修复时间")
    private String expectedTime;

    @Schema(description = "备注信息")
    private String remark;

    // 新增：设施ID（可选）
    @Schema(description = "设施ID", example = "1")
    private Long facilityId;
}
