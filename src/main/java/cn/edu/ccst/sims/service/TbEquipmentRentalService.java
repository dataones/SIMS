package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbEquipment;
import cn.edu.ccst.sims.entity.TbEquipmentRental;

import java.util.List;

public interface TbEquipmentRentalService {
    /**
     * 获取所有器材列表
     */
    Result<List<TbEquipment>> getAllEquipments();

    // 用户提交借用申请
    Result<Long> applyRental(Long userId, Long equipmentId, Integer count);

    // 查询我的借用记录
    Result<List<TbEquipmentRental>> myRentals(Long userId);

    // 管理员：查询全部借用记录
    Result<List<TbEquipmentRental>> listAll();

    // 管理员：审核通过
    Result<Void> approve(Long rentalId);

    // 管理员：驳回
    Result<Void> reject(Long rentalId);

    // 用户归还器材
    Result<Void> returnEquipment(Long rentalId, Long userId);
}
