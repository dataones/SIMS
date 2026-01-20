package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.dto.LoginDTO;
import cn.edu.ccst.sims.dto.RegisterDTO;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import cn.edu.ccst.sims.service.UserService;
import cn.edu.ccst.sims.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils; // Spring 自带 MD5

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // MD5 加密工具（Spring 自带）
    private String md5(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    @Override
    public String login(LoginDTO dto) {
        String md5Pass = md5(dto.getPassword());
        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>().eq("username", dto.getUsername()));
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (!user.getPassword().equals(md5Pass)) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void register(RegisterDTO dto) {
        // 校验密码一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("两次密码不一致");
        }

        // 用户名查重
        SysUser exist = userMapper.selectOne(new QueryWrapper<SysUser>().eq("username", dto.getUsername()));
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 手机号查重
        exist = userMapper.selectOne(new QueryWrapper<SysUser>().eq("phone", dto.getPhone()));
        if (exist != null) {
            throw new RuntimeException("手机号已被注册");
        }

        // email 查重（如果不为空）
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            exist = userMapper.selectOne(new QueryWrapper<SysUser>().eq("email", dto.getEmail()));
            if (exist != null) {
                throw new RuntimeException("邮箱已被注册");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(md5(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setNickname(
                dto.getNickname() == null || dto.getNickname().isBlank() ? dto.getUsername() : dto.getNickname());
        user.setRole(0);
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);
        userMapper.insert(user);
    }

    // 新增：获取用户详细信息
    public Map<String, Object> getUserInfo(Integer userId) {
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 构建返回的用户信息，注意排除敏感信息
        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("balance", user.getBalance());
        userInfo.put("status", user.getStatus());
        userInfo.put("createTime", user.getCreateTime());
        userInfo.put("updateTime", user.getUpdateTime());

        return userInfo;
    }

    @Override
    public Integer getUserIdFromToken(String token) {
        // 移除 "Bearer " 前缀（如果有）
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 使用您原有的JwtUtil获取用户ID，并转换为Integer
            Long userIdLong = jwtUtil.getUserId(token);
            return userIdLong != null ? userIdLong.intValue() : null;
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败");
        }
    }

    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        // 将明文密码进行MD5加密后与存储的密码比较
        String md5PlainPassword = md5(plainPassword);
        return md5PlainPassword.equals(hashedPassword);
    }

    @Override
    public void updatePassword(Integer userId, String newPassword) {
        if (userId == null || newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("参数不完整");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新密码
        user.setPassword(md5(newPassword.trim()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}