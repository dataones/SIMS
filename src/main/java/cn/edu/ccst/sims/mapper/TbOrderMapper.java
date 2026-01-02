package cn.edu.ccst.sims.mapper;

import cn.edu.ccst.sims.entity.TbOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 订单支付 Mapper 接口
 * 用于订单的CRUD，如查询用户订单列表、更新支付状态等。
 */

public interface TbOrderMapper extends BaseMapper<TbOrder> {
    // 统计平台所有已支付订单的总金额 (status = 1)
    @Select("SELECT IFNULL(SUM(amount), 0) FROM tb_order WHERE status = 1")
    BigDecimal selectTotalRevenue();

    // 统计指定用户已支付订单的总金额 (status = 1)
    @Select("SELECT IFNULL(SUM(amount), 0) FROM tb_order WHERE user_id = #{userId} AND status = 1")
    BigDecimal selectUserExpense(@Param("userId") Long userId);
}
