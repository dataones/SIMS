package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserManageService extends IService<SysUser> {

    /**
     * 更新用户
     */
    void updateUser(SysUser user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 切换用户状态
     */
    void toggleUserStatus(Long id, Integer status);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String password);
}
