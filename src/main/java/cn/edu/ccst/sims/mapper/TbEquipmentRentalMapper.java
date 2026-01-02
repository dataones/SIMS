package cn.edu.ccst.sims.mapper;

import cn.edu.ccst.sims.entity.TbEquipmentRental;
import cn.edu.ccst.sims.vo.RentalApprovalVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 器材借用记录 Mapper 接口
 * 用于借用记录的CRUD，如插入借用申请、更新归还时间等。
 */

public interface TbEquipmentRentalMapper extends BaseMapper<TbEquipmentRental> {
    // 查询所有申请中的借用 (status = 1)
    @Select("SELECT r.*, u.username, u.nickname, e.name as equipment_name " +
            "FROM tb_equipment_rental r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "LEFT JOIN tb_equipment e ON r.equipment_id = e.id " +
            "WHERE r.status = 1 " +
            "ORDER BY r.create_time DESC")
    List<RentalApprovalVO> selectPendingRentals();
}