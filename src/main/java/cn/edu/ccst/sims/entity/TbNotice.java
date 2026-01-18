package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    @TableId(type = IdType.AUTO)
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
     * 状态：0-草稿，1-已发布
     */
    private Integer status;
    /**
     * 优先级：1-高，2-中，3-低
     */
    private Integer priority;
    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;
    /**
     * 阅读量
     */
    private Integer readCount;
    /**
     * 过期时间，为空表示永久有效
     */
    private LocalDateTime expireTime;
}