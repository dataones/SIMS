package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.entity.TbEquipment;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface EquipmentService {
    Page<TbEquipment> listEquipments(Integer pageNum, Integer pageSize, String name, Integer status);
    void addEquipment(TbEquipment equipment);
    void updateEquipment(TbEquipment equipment);
    void deleteEquipment(Long id);
}