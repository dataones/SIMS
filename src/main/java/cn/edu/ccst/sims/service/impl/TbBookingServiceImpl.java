package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.dto.BookingDTO;
import cn.edu.ccst.sims.entity.TbBooking;
import cn.edu.ccst.sims.entity.TbOrder;
import cn.edu.ccst.sims.entity.TbVenue;
import cn.edu.ccst.sims.mapper.TbBookingMapper;
import cn.edu.ccst.sims.mapper.TbVenueMapper;
import cn.edu.ccst.sims.mapper.TbOrderMapper;
import cn.edu.ccst.sims.service.TbBookingService;
import cn.edu.ccst.sims.vo.BookingVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场馆预约服务实现类
 */
@Service
public class TbBookingServiceImpl extends ServiceImpl<TbBookingMapper, TbBooking> implements TbBookingService {

    @Autowired
    private TbBookingMapper bookingMapper;

    @Autowired
    private TbVenueMapper venueMapper;
    @Autowired
    private TbOrderMapper orderMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitBooking(BookingDTO bookingDTO, Long userId) {
        // 1. 参数校验
        validateBookingTime(bookingDTO.getStartTime(), bookingDTO.getEndTime());
        validateBookingDate(bookingDTO.getDate());

        // 2. 检查场馆是否存在且可用
        TbVenue venue = venueMapper.selectById(bookingDTO.getVenueId());
        if (venue == null) {
            throw new RuntimeException("场馆不存在");
        }
        if (venue.getStatus() != 1) {
            throw new RuntimeException("场馆维护中，暂不可预约");
        }

        // 3. 检查营业时间
        validateVenueOpenTime(venue, bookingDTO.getStartTime(), bookingDTO.getEndTime());

        // 4. 检查时间冲突
        if (checkTimeConflict(bookingDTO.getVenueId(), bookingDTO.getDate(),
                bookingDTO.getStartTime(), bookingDTO.getEndTime(), null)) {
            throw new RuntimeException("该时间段已被预约，请选择其他时间");
        }

        // 5. 计算费用
        BigDecimal totalPrice = calculateTotalPrice(bookingDTO.getVenueId(),
                bookingDTO.getStartTime(),
                bookingDTO.getEndTime());

        // 6. 创建预约记录
        // 6. 生成统一的订单号
        String orderNo = generateOrderNo();  // 使用BK开头的单号

// 创建预约记录
        TbBooking booking = new TbBooking();
        BeanUtils.copyProperties(bookingDTO, booking);
        booking.setOrderNo(orderNo);  // 预约记录也使用这个单号
        booking.setUserId(userId);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(0); // 待审核
        booking.setCreateTime(LocalDateTime.now());
        booking.setUpdateTime(LocalDateTime.now());

        save(booking);  // 保存预约记录

// 7. 创建订单记录
        TbOrder order = new TbOrder();
        order.setOrderNo(orderNo);  // 使用同一个单号
        order.setUserId(userId);
        order.setRelatedId(booking.getId());  // 关联预约ID
        order.setType(1);  // 1-场馆预约
        order.setAmount(totalPrice);
        order.setStatus(0);  // 0-未支付
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.insert(order);  // 保存订单记录

        return booking.getId();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBooking(BookingDTO bookingDTO, Long userId) {
        // 1. 检查预约是否存在
        TbBooking existingBooking = getById(bookingDTO.getId());
        if (existingBooking == null) {
            throw new RuntimeException("预约记录不存在");
        }

        // 2. 检查权限
        if (!existingBooking.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此预约");
        }

        // 3. 检查状态（只有待审核状态可以修改）
        if (existingBooking.getStatus() != 0) {
            throw new RuntimeException("只有待审核状态的预约可以修改");
        }

        // 4. 参数校验
        validateBookingTime(bookingDTO.getStartTime(), bookingDTO.getEndTime());
        validateBookingDate(bookingDTO.getDate());

        // 5. 检查场馆
        TbVenue venue = venueMapper.selectById(bookingDTO.getVenueId());
        if (venue == null || venue.getStatus() != 1) {
            throw new RuntimeException("场馆不可用");
        }

        // 6. 检查营业时间
        validateVenueOpenTime(venue, bookingDTO.getStartTime(), bookingDTO.getEndTime());

        // 7. 检查时间冲突（排除当前预约）
        if (checkTimeConflict(bookingDTO.getVenueId(), bookingDTO.getDate(),
                bookingDTO.getStartTime(), bookingDTO.getEndTime(), bookingDTO.getId())) {
            throw new RuntimeException("该时间段已被预约，请选择其他时间");
        }

        // 8. 重新计算费用
        BigDecimal totalPrice = calculateTotalPrice(bookingDTO.getVenueId(),
                bookingDTO.getStartTime(),
                bookingDTO.getEndTime());

        // 9. 更新预约记录
        TbBooking booking = new TbBooking();
        BeanUtils.copyProperties(bookingDTO, booking);
        booking.setTotalPrice(totalPrice);
        booking.setUpdateTime(LocalDateTime.now());

        updateById(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId, Long userId) {
        TbBooking booking = getById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("无权限取消此预约");
        }

        if (booking.getStatus() == 3) {
            throw new RuntimeException("预约已取消");
        }

        if (booking.getStatus() == 4) {
            throw new RuntimeException("预约已完成，无法取消");
        }

        // 检查是否可以取消（预约时间前2小时可以取消）
        LocalDateTime bookingDateTime = LocalDateTime.of(booking.getDate(),
                LocalTime.parse(booking.getStartTime()));
        if (LocalDateTime.now().plusHours(2).isAfter(bookingDateTime)) {
            throw new RuntimeException("预约开始前2小时内不可取消");
        }

        booking.setStatus(3); // 已取消
        booking.setUpdateTime(LocalDateTime.now());
        updateById(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBooking(Long bookingId, Integer status, String remark) {
        TbBooking booking = getById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }

        if (booking.getStatus() != 0) {
            throw new RuntimeException("只能审核待审核状态的预约");
        }

        if (status != 1 && status != 2) {
            throw new RuntimeException("审核状态错误");
        }

        booking.setStatus(status);
        booking.setUpdateTime(LocalDateTime.now());
        updateById(booking);
    }

    @Override
    public IPage<BookingVO> getBookingPage(Page<TbBooking> page, Long userId, Integer status, String venueName) {
        LambdaQueryWrapper<TbBooking> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(TbBooking::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(TbBooking::getStatus, status);
        }
        if (StringUtils.hasText(venueName)) {
            // 这里需要关联查询，在Mapper中实现
        }

        wrapper.orderByDesc(TbBooking::getCreateTime);

        IPage<TbBooking> bookingPage = page(page, wrapper);

        // 转换为VO
        IPage<BookingVO> voPage = new Page<>();
        BeanUtils.copyProperties(bookingPage, voPage);

        List<BookingVO> voList = bookingPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public List<BookingVO> getBookingsByUserId(Long userId) {
        List<TbBooking> bookings = bookingMapper.selectByUserId(userId);
        return bookings.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public BookingVO getBookingById(Long bookingId) {
        TbBooking booking = getById(bookingId);
        if (booking == null) {
            return null;
        }
        return convertToVO(booking);
    }

    @Override
    public boolean checkTimeConflict(Long venueId, LocalDate date, String startTime, String endTime, Long excludeId) {
        Long excludeBookingId = excludeId != null ? excludeId : -1L;
        int count = bookingMapper.checkTimeConflict(venueId, date, startTime, endTime, excludeBookingId);
        return count > 0;
    }

    @Override
    public List<String> getBookedTimeSlots(Long venueId, LocalDate date) {
        List<TbBooking> bookings = bookingMapper.selectByVenueAndDate(venueId, date);
        List<String> timeSlots = new ArrayList<>();

        for (TbBooking booking : bookings) {
            timeSlots.add(booking.getStartTime() + "-" + booking.getEndTime());
        }

        return timeSlots;
    }

    @Override
    public BigDecimal calculateTotalPrice(Long venueId, String startTime, String endTime) {
        TbVenue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new RuntimeException("场馆不存在");
        }

        // 计算时长（小时）
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        if (end.isBefore(start) || end.equals(start)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }

        long minutes = java.time.Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);

        return venue.getPrice()
                .multiply(hours)
                .multiply(new BigDecimal("1.1"));
    }

    /**
     * 生成预约单号
     */
    private String generateOrderNo() {
        return "BK" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 校验预约时间
     */
    private void validateBookingTime(String startTime, String endTime) {
        try {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);

            if (end.isBefore(start) || end.equals(start)) {
                throw new RuntimeException("结束时间必须晚于开始时间");
            }

            // 检查最小预约时长（1小时）
            long minutes = java.time.Duration.between(start, end).toMinutes();
            if (minutes < 60) {
                throw new RuntimeException("预约时长不能少于1小时");
            }

        } catch (Exception e) {
            throw new RuntimeException("时间格式错误");
        }
    }

    /**
     * 校验预约日期
     */
    private void validateBookingDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("不能预约过去的日期");
        }

        // 最多提前30天预约
        if (date.isAfter(LocalDate.now().plusDays(30))) {
            throw new RuntimeException("最多只能提前30天预约");
        }
    }

    /**
     * 校验场馆营业时间
     */
    private void validateVenueOpenTime(TbVenue venue, String startTime, String endTime) {
        LocalTime venueOpenTime = LocalTime.parse(venue.getOpenTime());
        LocalTime venueCloseTime = LocalTime.parse(venue.getCloseTime());
        LocalTime bookingStartTime = LocalTime.parse(startTime);
        LocalTime bookingEndTime = LocalTime.parse(endTime);

        if (bookingStartTime.isBefore(venueOpenTime) || bookingEndTime.isAfter(venueCloseTime)) {
            throw new RuntimeException(String.format("预约时间必须在场馆营业时间内（%s-%s）",
                    venue.getOpenTime(), venue.getCloseTime()));
        }
    }

    /**
     * 转换为VO对象
     */
    private BookingVO convertToVO(TbBooking booking) {
        BookingVO vo = new BookingVO();
        BeanUtils.copyProperties(booking, vo);

        // 查询场馆信息
        TbVenue venue = venueMapper.selectById(booking.getVenueId());
        if (venue != null) {
            vo.setVenueName(venue.getName());
            vo.setVenueType(venue.getType());
        }

        return vo;
    }
}