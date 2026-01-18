package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.*;
import cn.edu.ccst.sims.mapper.*;
import cn.edu.ccst.sims.vo.OrderVO;
import cn.edu.ccst.sims.service.TbOrderSettlementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TbOrderSettlementServiceImpl extends ServiceImpl<TbOrderMapper, TbOrder>
        implements TbOrderSettlementService {
    @Autowired
    private TbBookingMapper bookingMapper;

    @Autowired
    private TbEquipmentRentalMapper rentalMapper;
    private final TbOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final TbBookingMapper tbBookingMapper;
    private final TbVenueMapper tbVenueMapper;
    private final TbEquipmentMapper tbEquipmentMapper;
    private final TbEquipmentRentalMapper tbEquipmentRentalMapper;

    public TbOrderSettlementServiceImpl(TbOrderMapper orderMapper, SysUserMapper userMapper,
            TbBookingMapper tbBookingMapper, TbVenueMapper tbVenueMapper, TbEquipmentMapper tbEquipmentMapper,
            TbEquipmentRentalMapper tbEquipmentRentalMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
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
        System.out.println("处理支付: orderNo=" + orderNo + ", userId=" + userId);

        // 1. 首先尝试从 tb_order 表查询
        TbOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getOrderNo, orderNo)
                        .eq(TbOrder::getUserId, userId));

        if (order != null) {
            System.out.println("在 tb_order 表中找到订单，进行支付处理");
            return processExistingOrderPayment(order);
        }

        // 2. 如果 tb_order 表中没有，尝试从 tb_booking 表查询
        TbBooking booking = tbBookingMapper.selectOne(
                new LambdaQueryWrapper<TbBooking>()
                        .eq(TbBooking::getOrderNo, orderNo)
                        .eq(TbBooking::getUserId, userId));

        if (booking != null) {
            System.out.println("在 tb_booking 表中找到预约记录，进行支付处理");
            return processBookingPayment(booking);
        }

        // 3. 最后尝试从 tb_equipment_rental 表查询
        TbEquipmentRental rental = tbEquipmentRentalMapper.selectOne(
                new LambdaQueryWrapper<TbEquipmentRental>()
                        .eq(TbEquipmentRental::getOrderNo, orderNo)
                        .eq(TbEquipmentRental::getUserId, userId));

        if (rental != null) {
            System.out.println("在 tb_equipment_rental 表中找到租赁记录，进行支付处理");
            return processRentalPayment(rental);
        }

        System.out.println("所有表中都没有找到订单");
        return Result.error("订单不存在或状态异常");
    }

    // 处理已存在的订单支付
    private Result<Void> processExistingOrderPayment(TbOrder order) {
        if (order.getStatus() != 0) {
            return Result.error("订单不存在或状态异常");
        }

        // 更新订单为已支付
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 根据订单类型同步更新对应记录的状态
        Integer orderType = order.getType();
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

    // 处理预约支付
    private Result<Void> processBookingPayment(TbBooking booking) {
        if (booking.getStatus() != 1) {
            return Result.error("预约状态异常，无法支付。当前状态: " + booking.getStatus());
        }

        // 更新预约状态为已完成
        booking.setStatus(4); // 已完成
        booking.setUpdateTime(LocalDateTime.now());
        tbBookingMapper.updateById(booking);

        // 创建对应的订单记录到 tb_order 表
        TbOrder newOrder = new TbOrder();
        newOrder.setOrderNo(booking.getOrderNo());
        newOrder.setUserId(booking.getUserId());
        newOrder.setRelatedId(booking.getId());
        newOrder.setType(1); // 场馆预约
        newOrder.setAmount(booking.getTotalPrice());
        newOrder.setStatus(1); // 已支付
        newOrder.setPayTime(LocalDateTime.now());
        newOrder.setCreateTime(booking.getCreateTime());
        orderMapper.insert(newOrder);

        System.out.println("预约支付处理完成，订单号: " + booking.getOrderNo());
        return Result.success();
    }

    // 处理租赁支付
    private Result<Void> processRentalPayment(TbEquipmentRental rental) {
        if (rental.getStatus() != 1) {
            return Result.error("租赁状态异常，无法支付。当前状态: " + rental.getStatus());
        }

        // 更新租赁状态为使用中
        rental.setStatus(2); // 使用中
        rental.setUpdateTime(LocalDateTime.now());
        tbEquipmentRentalMapper.updateById(rental);

        // 创建对应的订单记录到 tb_order 表
        TbOrder newOrder = new TbOrder();
        newOrder.setOrderNo(rental.getOrderNo());
        newOrder.setUserId(rental.getUserId());
        newOrder.setRelatedId(rental.getId());
        newOrder.setType(2); // 器材租赁
        newOrder.setAmount(rental.getPrice());
        newOrder.setStatus(1); // 已支付
        newOrder.setPayTime(LocalDateTime.now());
        newOrder.setCreateTime(rental.getCreateTime());
        orderMapper.insert(newOrder);

        System.out.println("租赁支付处理完成，订单号: " + rental.getOrderNo());
        return Result.success();
    }

    /**
     * 订单完成（可评价）
     */
    @Override
    public Result<Void> finishOrder(String orderNo) {

        TbOrder order = orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getOrderNo, orderNo));

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
                        .eq(TbOrder::getOrderNo, orderNo));

        if (order == null || order.getStatus() != 1) {
            return Result.error("订单不可退款");
        }

        order.setStatus(4); // 已退款
        // 移除退款相关字段，因为数据库表中没有这些字段
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

    @Override
    public Result<TbOrder> getOrderDetail(Long userId, String orderNo) {
        try {
            System.out.println("查询订单详情: orderNo=" + orderNo + ", userId=" + userId);

            // 首先尝试从 tb_order 表查询
            TbOrder order = orderMapper.selectOne(
                    new LambdaQueryWrapper<TbOrder>()
                            .eq(TbOrder::getOrderNo, orderNo)
                            .eq(TbOrder::getUserId, userId));

            if (order != null) {
                System.out.println("在 tb_order 表中找到订单");
                return Result.success(order);
            }

            System.out.println("tb_order 表中没有找到，开始查询 tb_booking 表");

            // 如果 tb_order 表中没有，尝试从 tb_booking 表查询
            TbBooking booking = tbBookingMapper.selectOne(
                    new LambdaQueryWrapper<TbBooking>()
                            .eq(TbBooking::getOrderNo, orderNo)
                            .eq(TbBooking::getUserId, userId));

            if (booking != null) {
                System.out.println("在 tb_booking 表中找到预约记录");
                // 创建一个虚拟的 TbOrder 对象用于返回
                TbOrder virtualOrder = new TbOrder();
                virtualOrder.setOrderNo(booking.getOrderNo());
                virtualOrder.setUserId(booking.getUserId());
                virtualOrder.setRelatedId(booking.getId());
                virtualOrder.setType(1); // 场馆预约
                virtualOrder.setAmount(booking.getTotalPrice());
                virtualOrder.setStatus(booking.getStatus());
                virtualOrder.setCreateTime(booking.getCreateTime());
                virtualOrder.setPayTime(booking.getCreateTime()); // 使用创建时间作为支付时间
                return Result.success(virtualOrder);
            }

            System.out.println("tb_booking 表中没有找到，开始查询 tb_equipment_rental 表");

            // 最后尝试从 tb_equipment_rental 表查询
            TbEquipmentRental rental = tbEquipmentRentalMapper.selectOne(
                    new LambdaQueryWrapper<TbEquipmentRental>()
                            .eq(TbEquipmentRental::getOrderNo, orderNo)
                            .eq(TbEquipmentRental::getUserId, userId));

            if (rental != null) {
                System.out.println("在 tb_equipment_rental 表中找到租赁记录");
                // 创建一个虚拟的 TbOrder 对象用于返回
                TbOrder virtualOrder = new TbOrder();
                virtualOrder.setOrderNo(rental.getOrderNo());
                virtualOrder.setUserId(rental.getUserId());
                virtualOrder.setRelatedId(rental.getId());
                virtualOrder.setType(2); // 器材租赁
                virtualOrder.setAmount(rental.getPrice());
                virtualOrder.setStatus(rental.getStatus());
                virtualOrder.setCreateTime(rental.getCreateTime());
                virtualOrder.setPayTime(rental.getCreateTime()); // 使用创建时间作为支付时间
                return Result.success(virtualOrder);
            }

            System.out.println("所有表中都没有找到订单");
            return Result.error("订单不存在或无权限");
        } catch (Exception e) {
            System.out.println("查询订单详情异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("查询订单详情失败: " + e.getMessage());
        }
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

    @Override
    public Map<String, Object> calculateSettlement(cn.edu.ccst.sims.dto.OrderSettlementDTO settlementDTO) {
        try {
            Map<String, Object> result = new HashMap<>();

            // 1. 获取场馆信息
            TbVenue venue = tbVenueMapper.selectById(settlementDTO.getVenueId());
            if (venue == null) {
                throw new RuntimeException("场馆不存在");
            }

            // 2. 计算场地费用（基于时间段累加，支持高峰加价）
            BigDecimal basePrice = venue.getPrice() != null ? venue.getPrice() : new BigDecimal("100.00");
            BigDecimal venuePrice = calculateVenuePriceByTimeSlots(basePrice,
                    settlementDTO.getStartTime(), settlementDTO.getEndTime());

            // 3. 服务费：场地费用的10%
            BigDecimal serviceFee = venuePrice.multiply(new BigDecimal("0.1"));

            // 5. 总费用
            BigDecimal totalFee = venuePrice.add(serviceFee);

            // 6. 优惠金额（暂时为0，后续可扩展）
            BigDecimal discountAmount = BigDecimal.ZERO;

            // 7. 最终金额
            BigDecimal finalAmount = totalFee.subtract(discountAmount);

            // 8. 返回计算结果
            result.put("basePrice", basePrice);
            result.put("venuePrice", venuePrice);
            result.put("serviceFee", serviceFee);
            //
            //
            //
            // result.put("equipmentCost", equipmentCost);
            result.put("totalFee", totalFee);
            result.put("discountAmount", discountAmount);
            result.put("finalAmount", finalAmount);
            result.put("venueId", settlementDTO.getVenueId());
            result.put("bookingDate", settlementDTO.getBookingDate());
            result.put("startTime", settlementDTO.getStartTime());
            result.put("endTime", settlementDTO.getEndTime());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("费用计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据时间段计算场地费用（支持高峰时段加价）
     */
    private BigDecimal calculateVenuePriceByTimeSlots(BigDecimal basePrice, String startTime, String endTime) {
        java.time.LocalTime start = java.time.LocalTime.parse(startTime);
        java.time.LocalTime end = java.time.LocalTime.parse(endTime);

        BigDecimal venuePrice = BigDecimal.ZERO;
        java.time.LocalTime currentStart = start;

        while (currentStart.isBefore(end)) {
            java.time.LocalTime currentEnd = currentStart.plusHours(1);
            if (currentEnd.isAfter(end)) {
                currentEnd = end;
            }

            // 判断是否为高峰时段（18:00-21:00）
            int hour = currentStart.getHour();
            boolean isPeak = hour >= 18 && hour < 21;

            // 计算当前小时的费用
            BigDecimal hourPrice = isPeak ? basePrice.multiply(new BigDecimal("1.2")) : basePrice;
            venuePrice = venuePrice.add(hourPrice);

            currentStart = currentEnd;
        }

        return venuePrice.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public Map<String, Object> applyDiscount(String orderNo, String discountCode) {
        try {
            // 这里可以实现真实的优惠逻辑，目前简单处理
            LambdaQueryWrapper<TbOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbOrder::getOrderNo, orderNo);

            TbOrder order = orderMapper.selectOne(wrapper);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            Map<String, Object> result = new HashMap<>();

            // 简单的优惠逻辑：固定折扣
            BigDecimal discountAmount = new BigDecimal("25.00");
            BigDecimal finalAmount = order.getAmount().subtract(discountAmount);

            // 更新订单金额
            order.setAmount(finalAmount);
            orderMapper.updateById(order);

            result.put("discountAmount", discountAmount);
            result.put("finalAmount", finalAmount);
            result.put("discountCode", discountCode);
            result.put("originalAmount", order.getAmount());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("应用优惠失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void requestRefund(Long userId, String orderNo, String reason) {
        try {
            LambdaQueryWrapper<TbOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbOrder::getOrderNo, orderNo)
                    .eq(TbOrder::getUserId, userId);

            TbOrder order = orderMapper.selectOne(wrapper);
            if (order == null) {
                throw new RuntimeException("订单不存在或无权限");
            }

            // 更新订单状态为退款中
            order.setStatus(3); // 退款中
            orderMapper.updateById(order);

            System.out.println("用户 " + userId + " 申请退款 " + orderNo + ", 原因: " + reason);
        } catch (Exception e) {
            throw new RuntimeException("退款申请失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void processRefund(String orderNo, BigDecimal amount, String adminRemark) {
        try {
            LambdaQueryWrapper<TbOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbOrder::getOrderNo, orderNo);

            TbOrder order = orderMapper.selectOne(wrapper);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            // 更新订单状态为已退款
            order.setStatus(4); // 已退款
            // 移除退款相关字段，因为数据库表中没有这些字段
            orderMapper.updateById(order);

            System.out.println("处理退款 " + orderNo + ", 金额: " + amount + ", 备注: " + adminRemark);
        } catch (Exception e) {
            throw new RuntimeException("退款处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<OrderVO> listOrders(Long userId, Integer pageNum, Integer pageSize, Integer type, Integer status) {
        try {
            List<OrderVO> voList = new ArrayList<>();

            if (type == null) {
                type = 1; // 默认查询场馆预约
            }

            if (type == 1) {
                // ===== 查询场馆预约订单 =====
                LambdaQueryWrapper<TbBooking> bookingWrapper = new LambdaQueryWrapper<>();
                bookingWrapper.eq(TbBooking::getUserId, userId);

                if (status != null) {
                    bookingWrapper.eq(TbBooking::getStatus, status);
                }

                bookingWrapper.orderByDesc(TbBooking::getCreateTime);
                List<TbBooking> bookings = tbBookingMapper.selectList(bookingWrapper);

                for (TbBooking booking : bookings) {
                    OrderVO vo = new OrderVO();
                    vo.setType(1); // 场馆预约
                    TbVenue venue = tbVenueMapper.selectById(booking.getVenueId());
                    vo.setOrderNo(booking.getOrderNo());
                    vo.setVenueName(venue != null ? venue.getName() : "未知场馆");
                    vo.setVenueImage(venue != null ? venue.getImage() : null); // 设置场馆图片
                    vo.setVenueAddress(venue != null ? venue.getLocation() : "地址未知"); // 设置场馆地址
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
                rentalWrapper.eq(TbEquipmentRental::getUserId, userId);

                if (status != null) {
                    rentalWrapper.eq(TbEquipmentRental::getStatus, status);
                }

                rentalWrapper.orderByDesc(TbEquipmentRental::getCreateTime);
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
            }

            // 手动分页
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, voList.size());
            List<OrderVO> pageList = voList.subList(start, end);

            Page<OrderVO> resultPage = new Page<>(pageNum, pageSize);
            resultPage.setRecords(pageList);
            resultPage.setTotal(voList.size());

            return resultPage;
        } catch (Exception e) {
            throw new RuntimeException("查询订单列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getSettlementDetail(Long userId, String orderNo) {
        try {
            LambdaQueryWrapper<TbOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbOrder::getOrderNo, orderNo)
                    .eq(TbOrder::getUserId, userId);

            TbOrder order = orderMapper.selectOne(wrapper);
            if (order == null) {
                throw new RuntimeException("订单不存在或无权限");
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put("orderNo", order.getOrderNo());
            detail.put("amount", order.getAmount());
            detail.put("status", order.getStatus());
            detail.put("createTime", order.getCreateTime());
            detail.put("payTime", order.getPayTime());

            // 根据订单类型获取关联信息
            if (order.getType() == 1 && order.getRelatedId() != null) {
                // 场馆预约
                TbBooking booking = tbBookingMapper.selectById(order.getRelatedId());
                if (booking != null) {
                    TbVenue venue = tbVenueMapper.selectById(booking.getVenueId());
                    detail.put("venueName", venue != null ? venue.getName() : "未知场馆");
                    detail.put("date", booking.getDate());
                    detail.put("startTime", booking.getStartTime());
                    detail.put("endTime", booking.getEndTime());
                }
            } else if (order.getType() == 2 && order.getRelatedId() != null) {
                // 器材租赁
                TbEquipmentRental rental = tbEquipmentRentalMapper.selectById(order.getRelatedId());
                if (rental != null) {
                    TbEquipment equipment = tbEquipmentMapper.selectById(rental.getEquipmentId());
                    detail.put("equipmentName", equipment != null ? equipment.getName() : "未知器材");
                    detail.put("count", rental.getCount());
                }
            }

            return detail;
        } catch (Exception e) {
            throw new RuntimeException("查询结算详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<OrderVO> adminListOrders(Integer pageNum, Integer pageSize, Integer type, Integer status,
            String username) {
        try {
            Page<TbOrder> page = new Page<>(pageNum, pageSize);
            List<OrderVO> voList = new ArrayList<>();

            LambdaQueryWrapper<TbOrder> wrapper = new LambdaQueryWrapper<>();

            if (type != null) {
                wrapper.eq(TbOrder::getType, type);
            }

            if (status != null) {
                wrapper.eq(TbOrder::getStatus, status);
            }

            if (username != null && !username.trim().isEmpty()) {
                wrapper.like(TbOrder::getOrderNo, username);
            }

            wrapper.orderByDesc(TbOrder::getCreateTime);

            Page<TbOrder> orderPage = orderMapper.selectPage(page, wrapper);

            for (TbOrder order : orderPage.getRecords()) {
                OrderVO vo = new OrderVO();
                vo.setId(order.getId());
                vo.setOrderNo(order.getOrderNo());
                vo.setType(order.getType());
                vo.setAmount(order.getAmount());
                vo.setStatus(order.getStatus());
                vo.setCreateTime(order.getCreateTime());

                // 获取用户信息
                SysUser user = userMapper.selectById(order.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                }

                // 根据订单类型获取关联信息
                if (order.getType() == 1 && order.getRelatedId() != null) {
                    // 场馆预约
                    TbBooking booking = tbBookingMapper.selectById(order.getRelatedId());
                    if (booking != null) {
                        TbVenue venue = tbVenueMapper.selectById(booking.getVenueId());
                        vo.setVenueName(venue != null ? venue.getName() : "未知场馆");
                        vo.setDate(booking.getDate());
                        vo.setStartTime(booking.getStartTime());
                        vo.setEndTime(booking.getEndTime());
                    }
                } else if (order.getType() == 2 && order.getRelatedId() != null) {
                    // 器材租赁
                    TbEquipmentRental rental = tbEquipmentRentalMapper.selectById(order.getRelatedId());
                    if (rental != null) {
                        TbEquipment equipment = tbEquipmentMapper.selectById(rental.getEquipmentId());
                        vo.setEquipmentName(equipment != null ? equipment.getName() : "未知器材");
                        vo.setCount(rental.getCount());
                    }
                }

                voList.add(vo);
            }

            Page<OrderVO> resultPage = new Page<>(pageNum, pageSize);
            resultPage.setRecords(voList);
            resultPage.setTotal(orderPage.getTotal());

            return resultPage;
        } catch (Exception e) {
            throw new RuntimeException("管理员查询订单列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrderByOrderNo(String orderNo, Long userId) {
        // 根据订单号查找订单
        LambdaQueryWrapper<TbOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(TbOrder::getOrderNo, orderNo);
        TbOrder order = orderMapper.selectOne(orderWrapper);

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此订单");
        }

        // 检查订单状态，只有已取消、已驳回、已完成的订单可以删除
        if (order.getStatus() == 0 || order.getStatus() == 1) {
            System.out.println("订单状态是：" + order.getStatus());
            throw new RuntimeException("不能删除进行中的订单，只能删除已取消、已驳回或已完成的订单");
        }

        // 删除订单
        orderMapper.deleteById(order.getId());

        // 如果是场馆预约订单，同时删除关联的预约记录
        if (order.getType() == 1 && order.getRelatedId() != null) {
            bookingMapper.deleteById(order.getRelatedId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelUnpaidOrders(int minutes) {
        // 计算超时时间
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);

        // 查询超时的未支付订单，且对应的预约已通过审核
        LambdaQueryWrapper<TbOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(TbOrder::getStatus, 0) // 待支付状态
                .le(TbOrder::getCreateTime, cutoffTime) // 创建时间早于cutoffTime
                .eq(TbOrder::getType, 1) // 只处理场馆预约订单
                .isNotNull(TbOrder::getRelatedId); // 确保有关联的预约

        List<TbOrder> unpaidOrders = orderMapper.selectList(orderWrapper);

        int cancelledCount = 0;

        for (TbOrder order : unpaidOrders) {
            try {
                // 检查关联的预约是否已通过审核
                TbBooking booking = bookingMapper.selectById(order.getRelatedId());
                if (booking == null || booking.getStatus() != 1) {
                    log.debug("跳过订单 {}: 预约不存在或未通过审核", order.getOrderNo());
                    continue;
                }

                // 更新订单状态为已取消
                order.setStatus(3); // 已取消
                order.setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(order);

                // 同时更新预约状态
                booking.setStatus(3); // 已取消
                booking.setUpdateTime(LocalDateTime.now());
                bookingMapper.updateById(booking);

                cancelledCount++;

                // 记录日志
                log.info("自动取消超时未支付订单: {}, 创建时间: {}, 超时设置: {}分钟, 预约状态: 已通过",
                        order.getOrderNo(), order.getCreateTime(), minutes);

            } catch (Exception e) {
                log.error("自动取消订单失败: {}", order.getOrderNo(), e);
            }
        }

        return cancelledCount;
    }
}
