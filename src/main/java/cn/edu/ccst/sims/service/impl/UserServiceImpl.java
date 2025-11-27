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
import org.springframework.util.DigestUtils;  // Spring 自带 MD5

import java.math.BigDecimal;

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
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank() ? dto.getUsername() : dto.getNickname());
        user.setRole(0);
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);
        userMapper.insert(user);
    }
}