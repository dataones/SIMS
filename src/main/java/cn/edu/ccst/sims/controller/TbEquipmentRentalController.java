package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbEquipment;
import cn.edu.ccst.sims.entity.TbEquipmentRental;
import cn.edu.ccst.sims.service.TbEquipmentRentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TbEquipmentRentalController {

    private final TbEquipmentRentalService rentalService;


    public TbEquipmentRentalController(TbEquipmentRentalService rentalService) {
        this.rentalService = rentalService;
    }

    /** 用户提交借用申请 */
    @PostMapping("/equipment-rentals")
    public Result<Long> apply(@AuthenticationPrincipal Long userId,
                              @RequestParam @NotNull Long equipmentId,
                              @RequestParam @NotNull @Min(1) Integer count) {
        return rentalService.applyRental(userId, equipmentId, count);
    }

    /** 我的借用记录 */
    @GetMapping("/equipment-rentals/my")
    public Result<List<TbEquipmentRental>> my(@AuthenticationPrincipal Long userId) {
        return rentalService.myRentals(userId);
    }

    /** 管理员：借用列表 */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/equipment-rentals")
    public Result<List<TbEquipmentRental>> listAll() {
        return rentalService.listAll();
    }

    /** 管理员：审核通过 */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/equipment-rentals/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        return rentalService.approve(id);
    }

    /** 管理员：驳回 */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/equipment-rentals/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        return rentalService.reject(id);
    }

    /** 用户归还器材 */
    @PutMapping("/equipment-rentals/{orderNo}/return")
    public Result<Void> returnEquipment(@AuthenticationPrincipal Long userId,
                                        @PathVariable String orderNo) {
        System.out.print("传入的orderNo为"+orderNo);
        return rentalService.returnEquipment(orderNo, userId);
    }
    /** 获取所有器材列表 */
    @GetMapping("/equipment-list")
   public Result<List<TbEquipment>> getAllEquipments() {
        return rentalService.getAllEquipments();
    }
}
