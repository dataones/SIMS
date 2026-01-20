package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.dto.LoginDTO;
import cn.edu.ccst.sims.dto.RegisterDTO;

import java.util.Map;

/**
 * 用户服务接口
 * 
 * 负责处理用户相关的业务逻辑，包括：
 * 1. 用户认证 - 登录、注册、密码管理
 * 2. 用户信息 - 获取、更新用户资料
 * 3. 令牌管理 - JWT令牌解析和验证
 * 
 * @author SIMS开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface UserService {
    /**
     * 用户登录
     * 
     * @param dto 登录数据
     * @return JWT令牌
     */
    String login(LoginDTO dto);

    /**
     * 用户注册
     * 
     * @param dto 注册数据
     */
    void register(RegisterDTO dto);

    /**
     * 获取用户详细信息
     * 
     * @param userId 用户ID
     * @return 用户信息Map
     */
    Map<String, Object> getUserInfo(Integer userId);

    /**
     * 从JWT令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    Integer getUserIdFromToken(String token);

    /**
     * 验证用户密码
     * 
     * @param plainPassword  明文密码
     * @param hashedPassword 加密后的密码
     * @return 验证结果
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);

    /**
     * 更新用户密码
     * 
     * @param userId      用户ID
     * @param newPassword 新密码
     */
    void updatePassword(Integer userId, String newPassword);
}