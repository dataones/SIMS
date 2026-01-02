package cn.edu.ccst.sims.mapper;

import cn.edu.ccst.sims.entity.TbBooking;
import cn.edu.ccst.sims.vo.BookingApprovalVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 场馆预约Mapper接口
 */
@Mapper
public interface TbBookingMapper extends BaseMapper<TbBooking> {

    /**
     * 分页查询预约记录
     */
    @Select("SELECT b.*, u.nickname as userName, v.name as venueName " +
            "FROM tb_booking b " +
            "LEFT JOIN sys_user u ON b.user_id = u.id " +
            "LEFT JOIN tb_venue v ON b.venue_id = v.id " +
            "WHERE 1=1 " +
            "${ew.customSqlSegment}")
    IPage<TbBooking> selectBookingPage(Page<TbBooking> page, @Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<TbBooking> wrapper);

    /**
     * 检查时间冲突
     */
    @Select("SELECT COUNT(*) FROM tb_booking " +
            "WHERE venue_id = #{venueId} " +
            "AND date = #{date} " +
            "AND status IN (0, 1) " +
            "AND ((start_time < #{endTime} AND end_time > #{startTime})) " +
            "AND id != #{excludeId}")
    int checkTimeConflict(@Param("venueId") Long venueId,
                          @Param("date") LocalDate date,
                          @Param("startTime") String startTime,
                          @Param("endTime") String endTime,
                          @Param("excludeId") Long excludeId);

    /**
     * 根据用户ID查询预约记录
     */
    @Select("SELECT b.*, v.name as venueName, v.type as venueType " +
            "FROM tb_booking b " +
            "LEFT JOIN tb_venue v ON b.venue_id = v.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.create_time DESC")
    List<TbBooking> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据场馆ID和日期查询预约记录
     */
    @Select("SELECT * FROM tb_booking " +
            "WHERE venue_id = #{venueId} " +
            "AND date = #{date} " +
            "AND status IN (0, 1) " +
            "ORDER BY start_time")
    List<TbBooking> selectByVenueAndDate(@Param("venueId") Long venueId, @Param("date") LocalDate date);
    // 查询所有待审核的预约 (status = 0)
    @Select("SELECT b.*, u.username, u.nickname, v.name as venue_name " +
            "FROM tb_booking b " +
            "LEFT JOIN sys_user u ON b.user_id = u.id " +
            "LEFT JOIN tb_venue v ON b.venue_id = v.id " +
            "WHERE b.status = 0 " +
            "ORDER BY b.create_time DESC")
    List<BookingApprovalVO> selectPendingBookings();
}