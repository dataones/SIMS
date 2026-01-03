package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbEquipment;
import cn.edu.ccst.sims.service.EquipmentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    /**
     * ================= 公共接口 =================
     */

    /**
     * 器材列表查询
     * status: 传 1 查上架，传 0 查下架，不传查所有（根据Service逻辑调整）
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {

        // 调用 Service (假设你用了我上一个回答提供的 EquipmentService)
        Page<TbEquipment> page = equipmentService.listEquipments(pageNum, pageSize, name, status);
        return Result.success(page);
    }

    /**
     * ================= 管理员接口 =================
     */

    /**
     * 管理员新增器材
     */
    @PostMapping("/admin")
    public Result add(@RequestBody TbEquipment equipment) {
        equipmentService.addEquipment(equipment);
        return Result.success("添加成功");
    }

    /**
     * 管理员编辑器材 (包含上下架操作)
     * 上下架时前端只需传: { "id": 1, "status": 0 }
     */
    @PutMapping("/admin")
    public Result update(@RequestBody TbEquipment equipment) {
        equipmentService.updateEquipment(equipment);
        return Result.success("修改成功");
    }

    /**
     * 管理员删除器材
     */
    @DeleteMapping("/admin/{id}")
    public Result delete(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return Result.success("删除成功");
    }
}