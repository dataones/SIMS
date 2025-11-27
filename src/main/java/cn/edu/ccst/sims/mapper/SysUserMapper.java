package cn.edu.ccst.sims.mapper;

import cn.edu.ccst.sims.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 系统用户 Mapper 接口
 * 用于处理用户相关数据库操作，如登录验证、用户注册等。
 */

public interface SysUserMapper extends BaseMapper<SysUser> {
    // 如需自定义方法，可在此添加，例如：
    // SysUser selectByUsername(String username);
    // 但BaseMapper已足够，推荐用Wrapper在Service中实现。
}