package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.entity.TbBooking;
import cn.edu.ccst.sims.entity.TbEquipmentRental;
import cn.edu.ccst.sims.mapper.*;
import cn.edu.ccst.sims.vo.AdminStatsVO;
import cn.edu.ccst.sims.vo.UserStatsVO;
import cn.edu.ccst.sims.service.StatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private TbVenueMapper venueMapper;
    @Autowired
    private TbBookingMapper bookingMapper;
    @Autowired
    private TbEquipmentRentalMapper rentalMapper;
    @Autowired
    private TbOrderMapper orderMapper;

    @Override
    public AdminStatsVO getAdminStats() {
        AdminStatsVO vo = new AdminStatsVO();

        // 1. 注册用户总数
        vo.setTotalUsers(userMapper.selectCount(null));

        // 2. 场馆总数
        vo.setTotalVenues(venueMapper.selectCount(null));

        // 3. 待审批事项
        // 预约表: status=0 表示待审核
        QueryWrapper<TbBooking> bookingQ = new QueryWrapper<>();
        bookingQ.eq("status", 0);
        Long pendingBookings = bookingMapper.selectCount(bookingQ);

        // 借用表: status=1 表示申请中 (根据你的SQL注释)
        QueryWrapper<TbEquipmentRental> rentalQ = new QueryWrapper<>();
        rentalQ.eq("status", 1);
        Long pendingRentals = rentalMapper.selectCount(rentalQ);

        vo.setPendingApprovals(pendingBookings + pendingRentals);

        // 4. 平台总流水
        vo.setTotalRevenue(orderMapper.selectTotalRevenue());

        return vo;
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO vo = new UserStatsVO();

        // 1. 获取当前余额
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            vo.setBalance(user.getBalance());
        } else {
            vo.setBalance(BigDecimal.ZERO);
        }

        // 2. 我的预订数量
        QueryWrapper<TbBooking> bookingQ = new QueryWrapper<>();
        bookingQ.eq("user_id", userId);
        vo.setBookings(bookingMapper.selectCount(bookingQ));

        // 3. 我的借用数量
        QueryWrapper<TbEquipmentRental> rentalQ = new QueryWrapper<>();
        rentalQ.eq("user_id", userId);
        vo.setRentals(rentalMapper.selectCount(rentalQ));

        // 4. 累计消费
        vo.setTotalExpense(orderMapper.selectUserExpense(userId));

        return vo;
    }
}