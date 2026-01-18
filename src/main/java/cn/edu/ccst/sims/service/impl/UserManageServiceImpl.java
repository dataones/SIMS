package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import cn.edu.ccst.sims.service.UserManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class UserManageServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserManageService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void updateUser(SysUser user) {
        // 检查用户是否存在
        SysUser existingUser = this.getById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 如果修改了用户名，检查是否与其他用户冲突
        if (!existingUser.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<SysUser> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(SysUser::getUsername, user.getUsername())
                    .ne(SysUser::getId, user.getId());
            if (this.getOne(checkWrapper) != null) {
                throw new RuntimeException("用户名已存在");
            }
        }

        // 如果修改了邮箱，检查是否与其他用户冲突
        if (StringUtils.hasText(user.getEmail()) && !user.getEmail().equals(existingUser.getEmail())) {
            LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(SysUser::getEmail, user.getEmail())
                    .ne(SysUser::getId, user.getId());
            if (this.getOne(emailWrapper) != null) {
                throw new RuntimeException("邮箱已存在");
            }
        }

        // 如果修改了手机号，检查是否与其他用户冲突
        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().equals(existingUser.getPhone())) {
            LambdaQueryWrapper<SysUser> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(SysUser::getPhone, user.getPhone())
                    .ne(SysUser::getId, user.getId());
            if (this.getOne(phoneWrapper) != null) {
                throw new RuntimeException("手机号已存在");
            }
        }

        // 如果提供了新密码，则加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(existingUser.getPassword()); // 保持原密码
        }

        // 保持不变的字段
        user.setCreateTime(existingUser.getCreateTime());

        this.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否有关联数据（预约、订单等）
        // TODO: 添加业务逻辑检查

        this.removeById(id);
    }

    @Override
    public void toggleUserStatus(Long id, Integer status) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    public void resetPassword(Long id, String password) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!StringUtils.hasText(password)) {
            throw new RuntimeException("密码不能为空");
        }

        user.setPassword(passwordEncoder.encode(password));
        this.updateById(user);
    }
}
