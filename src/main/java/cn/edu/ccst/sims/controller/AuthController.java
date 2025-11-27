package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.LoginDTO;
import cn.edu.ccst.sims.dto.RegisterDTO;
import cn.edu.ccst.sims.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
}