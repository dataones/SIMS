package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告表，对应通知发布、通知展示
 */
@Data
@TableName("tb_notice")
public class TbNotice {
    /**
     * 公告ID
     */
    @TableId
    private Long id;
    /**
     * 公告标题
     */
    private String title;
    /**
     * 公告内容
     */
    private String content;
    /**
     * 发布人
     */
    private String createBy;
    /**
     * 发布时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}