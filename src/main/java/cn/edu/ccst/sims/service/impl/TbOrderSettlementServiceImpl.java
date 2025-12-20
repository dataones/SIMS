package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.*;
import cn.edu.ccst.sims.mapper.*;
import cn.edu.ccst.sims.ov.OrderVO;
import cn.edu.ccst.sims.service.TbOrderSettlementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TbOrderSettlementServiceImpl extends ServiceImpl<TbOrderMapper, TbOrder> implements TbOrderSettlementService {
    @Autowired
    private TbBookingMapper bookingMapper;

    @Autowired
    private TbEquipmentRentalMapper rentalMapper;
    private final TbOrderMapper orderMapper;
    //private final SysUserMapper sysUserMapper;
    private final TbBookingMapper tbBookingMapper;
    private final TbVenueMapper tbVenueMapper;
    private final TbEquipmentMapper tbEquipmentMapper;
    private final TbEquipmentRentalMapper tbEquipmentRentalMapper;
    public TbOrderSettlementServiceImpl(TbOrderMapper orderMapper, SysUserMapper sysUserMapper, TbBookingMapper tbBookingMapper, TbVenueMapper tbVenueMapper, TbEquipmentMapper tbEquipmentMapper, TbEquipmentRentalMapper tbEquipmentRentalMapper) {
        this.orderMapper = orderMapper;
        //this.sysUserMapper = sysUserMapper;
        this.tbBookingMapper = tbBookingMapper;
        this.tbVenueMapper = tbVenueMapper;
        this.tbEquipmentMapper = tbEquipmentMapper;
        this.tbEquipmentRentalMapper = tbEquipmentRentalMapper;
    }

    /**
     * 创建订单并计算费用
     */
    @Override
    @Transactional
    public Result<Long> createOrder(Long userId,
                                    Integer type,
                                    Long relatedId,
                                    BigDecimal baseAmount) {

        // 示例：统一 9 折
        BigDecimal discountRate = new BigDecimal("0.9");
        BigDecimal finalAmount = baseAmount.multiply(discountRate);

        TbOrder order = new TbOrder();
        order.setOrderNo(generateOrderNo(type));
        order.setUserId(userId);
        order.setRelatedId(relatedId);
        order.setType(type);
        order.setAmount(finalAmount);
        order.setStatus(0); // 待支付
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);
        return Result.success(order.getId());
    }

    /**
     * 支付订单
     */
    @Override
    @Transactional
    public Result<Void> payOrder(Long userId, String orderNo) {

        // 1. 查询订单
        TbOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getOrderNo, orderNo)
                        .eq(TbOrder::getUserId, userId)
        );

        if (order == null || order.getStatus() != 0) {
            return Result.error("订单不存在或状态异常");
        }

        // 2. 更新订单为已支付
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 3. 根据订单类型同步更新对应记录的状态
        Integer orderType = order.getType(); // 1-场馆预约, 2-器材租赁
        System.out.println("orderType:" + orderType);
        Long relatedId = order.getRelatedId();

        if (orderType == 1 && relatedId != null) {
            // 场馆预约订单：更新tb_booking状态
            TbBooking booking = bookingMapper.selectById(relatedId);
            if (booking != null) {
                booking.setStatus(4); // 已通过
                booking.setUpdateTime(LocalDateTime.now());
                bookingMapper.updateById(booking);
            }
        } else if (orderType == 2 && relatedId != null) {
            // 器材租赁订单：更新tb_equipment_rental状态
            TbEquipmentRental rental = rentalMapper.selectById(relatedId);
            if (rental != null) {
                rental.setStatus(2); // 使用中
                rental.setUpdateTime(LocalDateTime.now());
                rentalMapper.updateById(rental);
            }
        }

        return Result.success();
    }

    /**
     * 订单完成（可评价）
     */
    @Override
    public Result<Void> finishOrder(String orderNo) {

        TbOrder order = orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getOrderNo, orderNo)
        );

        if (order == null || order.getStatus() != 1) {
            return Result.error("订单无法完成");
        }

        order.setStatus(2); // 已完成
        orderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 退款
     */
    @Override
    public Result<Void> refundOrder(String orderNo, BigDecimal refundAmount) {

        TbOrder order = orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getOrderNo, orderNo)
        );

        if (order == null || order.getStatus() != 1) {
            return Result.error("订单不可退款");
        }

        order.setStatus(4); // 已退款
        orderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 生成订单号：EQ202412150001
     */
    private String generateOrderNo(Integer type) {
        String prefix = (type == 1) ? "VN" : "EQ";
        long number = System.currentTimeMillis() % 1000000;
        return prefix + number;
    }
    /** 查询订单详情 */
    @Override
    public Result<TbOrder> getOrderDetail(Long userId, String orderNo) {

        TbOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getOrderNo, orderNo)
                        .eq(TbOrder::getUserId, userId)
        );

        if (order == null) {
            return Result.error("订单不存在或无权限");
        }

        return Result.success(order);
    }
    @Override
    public Result<List<OrderVO>> listOrders(Long userId, Integer type) {
        if (type == null) {
            type = 1; // 默认查询场馆预约
        }

        List<OrderVO> voList = new ArrayList<>();

        if (type == 1) {
            // ===== 查询场馆预约订单 =====
            LambdaQueryWrapper<TbBooking> bookingWrapper = new LambdaQueryWrapper<>();
            bookingWrapper.eq(TbBooking::getUserId, userId)
                    .orderByDesc(TbBooking::getCreateTime);
            List<TbBooking> bookings = tbBookingMapper.selectList(bookingWrapper);

            for (TbBooking booking : bookings) {
                OrderVO vo = new OrderVO();
                vo.setType(1); // 场馆预约
                TbVenue venue = tbVenueMapper.selectById(booking.getVenueId());
                vo.setOrderNo(booking.getOrderNo());
                vo.setVenueName(venue != null ? venue.getName() : "未知场馆");
                vo.setDate(booking.getDate());
                vo.setStartTime(booking.getStartTime());
                vo.setEndTime(booking.getEndTime());
                vo.setAmount(booking.getTotalPrice());
                vo.setStatus(booking.getStatus());
                vo.setCreateTime(booking.getCreateTime());
                voList.add(vo);
            }
        } else if (type == 2) {
            // ===== 查询器材租赁订单 =====
            LambdaQueryWrapper<TbEquipmentRental> rentalWrapper = new LambdaQueryWrapper<>();
            rentalWrapper.eq(TbEquipmentRental::getUserId, userId)
                    .orderByDesc(TbEquipmentRental::getCreateTime);
            List<TbEquipmentRental> rentals = tbEquipmentRentalMapper.selectList(rentalWrapper);

            for (TbEquipmentRental rental : rentals) {
                OrderVO vo = new OrderVO();
                vo.setType(2); // 器材租赁
                TbEquipment equipment = tbEquipmentMapper.selectById(rental.getEquipmentId());
                vo.setEquipmentName(equipment != null ? equipment.getName() : "未知器材");
                vo.setOrderNo(rental.getOrderNo());
                vo.setCount(rental.getCount());
                vo.setAmount(rental.getPrice());
                vo.setStatus(rental.getStatus());
                vo.setCreateTime(rental.getCreateTime());
                voList.add(vo);
            }
        } else {
            return Result.error("不支持的订单类型: " + type);
        }

        return Result.success(voList);
    }



}
