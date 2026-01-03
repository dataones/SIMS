package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbEquipment;
import cn.edu.ccst.sims.entity.TbEquipmentRental;
import cn.edu.ccst.sims.entity.TbOrder;
import cn.edu.ccst.sims.mapper.TbEquipmentMapper;
import cn.edu.ccst.sims.mapper.TbEquipmentRentalMapper;
import cn.edu.ccst.sims.mapper.TbOrderMapper;
import cn.edu.ccst.sims.service.TbEquipmentRentalService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TbEquipmentRentalServiceImpl implements TbEquipmentRentalService {

    private final TbEquipmentRentalMapper rentalMapper;
    private final TbEquipmentMapper equipmentMapper;
    private final TbOrderMapper orderMapper;

    public TbEquipmentRentalServiceImpl(TbEquipmentRentalMapper rentalMapper,
                                        TbEquipmentMapper equipmentMapper,
                                        TbOrderMapper orderMapper) {
        this.rentalMapper = rentalMapper;
        this.equipmentMapper = equipmentMapper;
        this.orderMapper = orderMapper;
    }

    /**
     * 用户提交借用申请
     */
    @Override
    @Transactional
    public Result<Long> applyRental(Long userId, Long equipmentId, @Min(1) Integer count) {

        if (count == null || count <= 0) {
            return Result.error("租用数量必须大于0");
        }

        TbEquipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            return Result.error("器材不存在");
        }

        int available = equipment.getTotalStock() - equipment.getRentedStock();
        if (count > available) {
            return Result.error("库存不足");
        }

        // 计算总费用
        BigDecimal totalPrice = equipment.getPrice().multiply(BigDecimal.valueOf(count));

        // 新增借用记录
        TbEquipmentRental rental = new TbEquipmentRental();
        String orderNo = "EQ" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
        rental.setUserId(userId);
        rental.setEquipmentId(equipmentId);
        rental.setCount(count);
        rental.setPrice(totalPrice);  // 使用计算好的totalPrice
        rental.setStatus(1); // 申请中
        rental.setOrderNo(orderNo);  // 借用记录也使用订单号
        rental.setCreateTime(LocalDateTime.now());
        rental.setUpdateTime(LocalDateTime.now());
        rentalMapper.insert(rental);

        // 创建订单（使用同一个单号）
        TbOrder order = new TbOrder();
        order.setOrderNo(orderNo);  // 使用同一个单号
        order.setUserId(userId);
        order.setRelatedId(rental.getId());
        order.setType(2); // 器材租赁
        order.setAmount(totalPrice);  // 使用计算好的totalPrice
        order.setStatus(0); // 未支付
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        return Result.success(rental.getId());
    }
    @Override
    public Result<List<TbEquipment>> getAllEquipments() {
        try {
            LambdaQueryWrapper<TbEquipment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(TbEquipment::getCreateTime);
            List<TbEquipment> equipments = equipmentMapper.selectList(queryWrapper);  // 使用 equipmentMapper
            return Result.success(equipments);
        } catch (Exception e) {
            return Result.error("查询器材列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询我的借用记录
     */
    @Override
    public Result<List<TbEquipmentRental>> myRentals(Long userId) {
        List<TbEquipmentRental> list = rentalMapper.selectList(
                new LambdaQueryWrapper<TbEquipmentRental>()
                        .eq(TbEquipmentRental::getUserId, userId)
                        .orderByDesc(TbEquipmentRental::getCreateTime)
        );
        return Result.success(list);
    }

    /**
     * 管理员查询全部
     */
    @Override
    public Result<List<TbEquipmentRental>> listAll() {
        return Result.success(rentalMapper.selectList(null));
    }

    /**
     * 管理员审核通过
     */
    @Override
    @Transactional
    public Result<Void> approve(Long rentalId) {

        TbEquipmentRental rental = rentalMapper.selectById(rentalId);
        if (rental == null || rental.getStatus() != 1) {
            return Result.error("借用记录不存在或状态不合法");
        }

        TbEquipment equipment = equipmentMapper.selectById(rental.getEquipmentId());
        int available = equipment.getTotalStock() - equipment.getRentedStock();
        if (rental.getCount() > available) {
            return Result.error("库存不足，无法审核通过");
        }

        // 扣库存（乐观锁）
        LambdaUpdateWrapper<TbEquipment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TbEquipment::getId, equipment.getId())
                .eq(TbEquipment::getRentedStock, equipment.getRentedStock())
                .set(TbEquipment::getRentedStock, equipment.getRentedStock() + rental.getCount());

        if (equipmentMapper.update(null, updateWrapper) != 1) {
            return Result.error("库存更新失败，请重试");
        }

        // 更新借用状态
        rental.setStatus(2); // 使用中
        rentalMapper.updateById(rental);

        // 同步订单状态为已支付
        TbOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getRelatedId, rentalId)
                        .eq(TbOrder::getType, 2)
        );
        if (order != null) {
            order.setStatus(1); // 已支付
            orderMapper.updateById(order);
        }

        return Result.success();
    }

    /**
     * 管理员驳回
     */
    @Override
    @Transactional
    public Result<Void> reject(Long rentalId) {

        TbEquipmentRental rental = rentalMapper.selectById(rentalId);
        if (rental == null || rental.getStatus() != 1) {
            return Result.error("借用记录不存在或状态不合法");
        }

        rental.setStatus(4); // 已驳回
        rentalMapper.updateById(rental);

        // 订单取消
        TbOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getRelatedId, rentalId)
                        .eq(TbOrder::getType, 2)
        );
        if (order != null) {
            order.setStatus(3); // 已取消
            orderMapper.updateById(order);
        }

        return Result.success();
    }

    /**
     * 用户归还器材
     */
    /**
     * 用户归还器材（按订单号）
     */
    @Override
    @Transactional
    public Result<Void> returnEquipment(String orderNo, Long userId) {

        // 1. 先通过订单号找到借用记录：tb_equipment_rental.order_no
        TbEquipmentRental rental = rentalMapper.selectOne(
                new LambdaQueryWrapper<TbEquipmentRental>()
                        .eq(TbEquipmentRental::getOrderNo, orderNo)
        );
        if (rental == null || !rental.getUserId().equals(userId)) {
            return Result.error("借用记录不存在或不属于当前用户");
        }

        // 2. 校验状态
        if (rental.getStatus() != 2) {
            return Result.error("借用状态不允许归还");
        }

        // 3. 查器材
        TbEquipment equipment = equipmentMapper.selectById(rental.getEquipmentId());
        if (equipment == null) {
            return Result.error("器材不存在");
        }

        // 4. 减库存（乐观锁）
        LambdaUpdateWrapper<TbEquipment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TbEquipment::getId, equipment.getId())
                .eq(TbEquipment::getRentedStock, equipment.getRentedStock())
                .set(TbEquipment::getRentedStock, equipment.getRentedStock() - rental.getCount());

        if (equipmentMapper.update(null, updateWrapper) != 1) {
            return Result.error("库存更新失败，请重试");
        }

        // 5. 更新借用状态为“已归还”
        rental.setStatus(3); // 已归还
        rentalMapper.updateById(rental);

        // 6. 同步订单状态（用 related_id = rental.id 关联）
        TbOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<TbOrder>()
                        .eq(TbOrder::getRelatedId, rental.getId()) // 注意这里用 rental.getId()
                        .eq(TbOrder::getType, 2)
        );
        if (order != null) {
            order.setStatus(1); // 已支付，保持订单已支付
            orderMapper.updateById(order);
        }

        return Result.success();
    }
}
