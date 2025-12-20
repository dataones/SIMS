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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // 临时允许跨域，前端开发用
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private SysUserMapper userMapper;
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>().eq("username", dto.getUsername()));
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }
    // 新增：获取用户信息接口
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

        userMapper.updateById(user);
        return Result.success("更新成功");
    }
}