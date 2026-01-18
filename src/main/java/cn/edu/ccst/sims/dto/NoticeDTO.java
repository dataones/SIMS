package cn.edu.ccst.sims.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告DTO
 */
@Data
@Schema(description = "公告信息")
public class NoticeDTO {

    @NotBlank(message = "公告标题不能为空")
    @Schema(description = "公告标题", example = "系统维护通知")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Schema(description = "公告内容", example = "系统将于今晚22:00进行维护...")
    private String content;

    @Schema(description = "公告类型", example = "SYSTEM")
    private String type; // SYSTEM, MAINTENANCE, ACTIVITY, URGENT

    @Schema(description = "是否置顶", example = "false")
    private Boolean isTop = false;

    @Schema(description = "优先级：1-高，2-中，3-低", example = "2")
    private Integer priority = 2;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "目标用户类型", example = "ALL")
    private String targetUserType; // ALL, USER, ADMIN, MEMBER

    @Schema(description = "公告图片URL")
    private String imageUrl;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive = true;

    @Schema(description = "备注信息")
    private String remark;
}
