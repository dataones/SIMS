package cn.edu.ccst.sims.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 系统用户表，对应用户登录/注册、个人中心、管理员权限
 */
@Data
@TableName("sys_user")
public class SysUser {
    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 登录账号
     */
    private String username;
    /**
     * 登录密码(MD5或其他加密)
     */
    private String password;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 头像地址
     */
    private String avatar;
    /**
     * 角色: 0-普通用户, 1-会员, 2-管理员
     */
    private Integer role;
    /**
     * 会员余额
     */
    private BigDecimal balance;
    /**
     * 状态: 1-正常, 0-禁用
     */
    private Integer status;
    /**
     * 注册时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 邮箱
     */
    private String email;
}