package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.entity.TbEquipment;
import cn.edu.ccst.sims.mapper.TbEquipmentMapper; // 需自行创建Mapper接口继承BaseMapper
import cn.edu.ccst.sims.service.EquipmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private TbEquipmentMapper equipmentMapper;

    @Override
    public Page<TbEquipment> listEquipments(Integer pageNum, Integer pageSize, String name, Integer status) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        QueryWrapper<TbEquipment> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        // 这里的逻辑和你Venue保持一致：若不传status，默认查上架的
        // 如果是后台管理，建议前端显式传 null 或特定值来控制
        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        return equipmentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void addEquipment(TbEquipment equipment) {
        // 设置默认值
        if (equipment.getRentedStock() == null) equipment.setRentedStock(0);
        if (equipment.getStatus() == null) equipment.setStatus(1);

        equipmentMapper.insert(equipment);
    }

    @Override
    public void updateEquipment(TbEquipment equipment) {
        if (equipment.getId() == null) {
            throw new RuntimeException("ID不能为空");
        }
        equipmentMapper.updateById(equipment);
    }

    @Override
    public void deleteEquipment(Long id) {
        equipmentMapper.deleteById(id);
    }
}