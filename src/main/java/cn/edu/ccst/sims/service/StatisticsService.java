package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.vo.AdminStatsVO;
import cn.edu.ccst.sims.vo.UserStatsVO;

public interface StatisticsService {
    // 获取管理员统计数据
    AdminStatsVO getAdminStats();

    // 获取指定用户的统计数据
    UserStatsVO getUserStats(Long userId);
}