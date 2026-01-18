package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设施故障报修表，对应故障申报/设施管理
 */
@Data
@TableName("tb_fault_report")
public class TbFaultReport {
    /**
     * 报修ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 申报人ID
     */
    private Long userId;
    /**
     * 关联场馆ID(可选)
     */
    private Long venueId;
    /**
     * 故障标题
     */
    private String title;
    /**
     * 故障详情
     */
    private String content;
    /**
     * 紧急程度: LOW-低, MEDIUM-中, HIGH-高
     */
    private String urgency;
    /**
     * 故障位置
     */
    private String location;
    /**
     * 故障类型
     */
    private String faultType;
    /**
     * 状态: 0-待处理, 1-处理中, 2-已修复
     */
    private Integer status;
    /**
     * 处理结果反馈
     */
    private String result;
    /**
     * 申报时间
     */
    private LocalDateTime createTime;
}