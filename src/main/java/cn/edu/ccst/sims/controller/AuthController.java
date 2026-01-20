package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.LoginDTO;
import cn.edu.ccst.sims.dto.RegisterDTO;
import cn.edu.ccst.sims.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 * 
 * 负责处理用户登录、注册、信息获取等认证相关操作
 * 
 * 功能模块：
 * 1. 用户登录 - JWT令牌生成
 * 2. 用户注册 - 账户创建
 * 3. 用户信息 - 获取当前用户详情
 * 4. 用户登出 - 令牌失效处理
 * 
 * 安全机制：
 * - JWT令牌认证
 * - 密码MD5加密存储
 * - 参数校验(@Valid)
 * - 异常统一处理
 * 
 * @author SIMS开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:3000" }, allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 用户登录接口
     * 
     * 处理用户登录请求，验证用户名和密码，生成JWT令牌
     * 
     * @param dto 登录请求参数，包含用户名和密码
     * @return 登录结果，包含JWT令牌和用户基本信息
     * 
     *         请求示例：
     *         POST /api/auth/login
     *         {
     *         "username": "admin",
     *         "password": "123456"
     *         }
     * 
     *         响应示例：
     *         {
     *         "code": 200,
     *         "msg": "成功",
     *         "data": {
     *         "token": "eyJhbGciOiJIUzI1NiJ9...",
     *         "userId": 1,
     *         "username": "admin",
     *         "nickname": "管理员",
     *         "role": 2
     *         }
     *         }
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDTO dto) {
        try {
            // 调用UserService进行登录验证，返回JWT令牌
            String token = userService.login(dto);

            // 查询用户完整信息
            SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>().eq("username", dto.getUsername()));

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token); // JWT令牌，用于后续请求认证
            data.put("userId", user.getId()); // 用户ID
            data.put("username", user.getUsername()); // 用户名
            data.put("nickname", user.getNickname()); // 昵称
            data.put("role", user.getRole()); // 用户角色：0-普通用户，1-会员，2-管理员

            return Result.success(data);
        } catch (RuntimeException e) {
            // 业务异常（如用户名不存在、密码错误等）
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 系统异常
            e.printStackTrace();
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户注册接口
     * 
     * 处理新用户注册请求，创建用户账户
     * 
     * @param dto 注册请求参数，包含用户名、密码、昵称等
     * @return 注册结果
     * 
     *         请求示例：
     *         POST /api/auth/register
     *         {
     *         "username": "newuser",
     *         "password": "123456",
     *         "nickname": "新用户",
     *         "phone": "13800138000",
     *         "email": "user@example.com"
     *         }
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }

    /**
     * 获取用户信息接口
     * 
     * 处理获取当前用户信息请求，返回用户详细信息
     * 
     * @param userId 当前用户ID
     * @return 用户信息
     * 
     *         请求示例：
     *         GET /api/auth/user-info
     * 
     *         响应示例：
     *         {
     *         "code": 200,
     *         "msg": "成功",
     *         "data": {
     *         "userId": 1,
     *         "username": "admin",
     *         "nickname": "管理员",
     *         "role": 2,
     *         "phone": "13800138000",
     *         "email": "admin@example.com"
     *         }
     *         }
     */
    @GetMapping("/user-info")
    public Result getUserInfo(@AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未授权访问");
            }

            // 获取用户详细信息
            Map<String, Object> userInfo = userService.getUserInfo(userId.intValue());
            return Result.success(userInfo);

        } catch (Exception e) {
            return Result.error(401, "Token无效或已过期");
        }
    }

    // 新增：更新用户信息接口（可选）
    @PutMapping("/user/update")
    public Result updateUserInfo(@RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> updateData) {
        Integer userId = userService.getUserIdFromToken(token);
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新允许修改的字段
        if (updateData.containsKey("nickname")) {
            user.setNickname(updateData.get("nickname"));
        }
        if (updateData.containsKey("email")) {
            user.setEmail(updateData.get("email"));
        }
        if (updateData.containsKey("phone")) {
            // 这里可以添加手机号重复校验
            user.setPhone(updateData.get("phone"));
        }
        if (updateData.containsKey("avatar")) {
            user.setAvatar(updateData.get("avatar"));
        }

        userMapper.updateById(user);
        return Result.success("更新成功");
    }

    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }

    /**
     * 修改密码接口
     * 
     * 处理用户修改密码请求
     * 
     * @param token        JWT令牌，用于身份验证
     * @param passwordData 修改密码数据
     * @param {String}     passwordData.oldPassword - 当前密码
     * @param {String}     passwordData.newPassword - 新密码
     * @return 修改结果
     * 
     *         请求示例：
     *         PUT /api/auth/change-password
     *         {
     *         "oldPassword": "123456",
     *         "newPassword": "654321"
     *         }
     * 
     *         响应示例：
     *         {
     *         "code": 200,
     *         "msg": "密码修改成功",
     *         "data": null
     *         }
     */
    @PutMapping("/change-password")
    public Result changePassword(@RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> passwordData) {
        try {
            Integer userId = userService.getUserIdFromToken(token);
            SysUser user = userMapper.selectById(userId);

            if (user == null) {
                return Result.error("用户不存在");
            }

            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return Result.error("参数不完整");
            }

            // 验证当前密码
            if (!userService.verifyPassword(oldPassword, user.getPassword())) {
                return Result.error("当前密码错误");
            }

            // 更新密码
            userService.updatePassword(userId, newPassword);

            return Result.success("密码修改成功");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("密码修改失败：" + e.getMessage());
        }
    }
}