package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.dto.BookingDTO;
import cn.edu.ccst.sims.entity.TbBooking;
import cn.edu.ccst.sims.ov.BookingVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 场馆预约服务接口
 */
public interface TbBookingService extends IService<TbBooking> {

    /**
     * 提交场馆预约
     */
    Long submitBooking(BookingDTO bookingDTO, Long userId);

    /**
     * 修改场馆预约
     */
    void updateBooking(BookingDTO bookingDTO, Long userId);

    /**
     * 取消预约
     */
    void cancelBooking(Long bookingId, Long userId);

    /**
     * 管理员审核预约
     */
    void auditBooking(Long bookingId, Integer status, String remark);

    /**
     * 分页查询预约记录
     */
    IPage<BookingVO> getBookingPage(Page<TbBooking> page, Long userId, Integer status, String venueName);

    /**
     * 根据用户ID查询预约记录
     */
    List<BookingVO> getBookingsByUserId(Long userId);

    /**
     * 根据预约ID查询详情
     */
    BookingVO getBookingById(Long bookingId);

    /**
     * 检查时间冲突
     */
    boolean checkTimeConflict(Long venueId, LocalDate date, String startTime, String endTime, Long excludeId);

    /**
     * 获取场馆某日期的预约时间段
     */
    List<String> getBookedTimeSlots(Long venueId, LocalDate date);

    /**
     * 计算预约费用
     */
    BigDecimal calculateTotalPrice(Long venueId, String startTime, String endTime);
}