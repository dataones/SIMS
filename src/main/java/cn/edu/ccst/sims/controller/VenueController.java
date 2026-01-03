package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.VenueDTO;
import cn.edu.ccst.sims.entity.TbVenue;
import cn.edu.ccst.sims.service.VenueService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

    /**
     * ================= 公共接口 =================
     */

    /**
     * 场馆列表查询（支持分页 + 搜索）
     * status：
     *   - 不传：默认只查正常场馆
     *   - 传 0 / 1：按状态查询（管理员可用）
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {

        Page<VenueDTO> page =
                venueService.listVenues(pageNum, pageSize, type, name, status);
        return Result.success(page);
    }
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        System.out.print("调用了");
        VenueDTO dto = venueService.getVenueDetail(id);
        System.out.print(dto);
        return Result.success(dto);
    }
    /**
     * ================= 管理员接口 =================
     */

    /**
     * 管理员查看场馆详情（编辑回显）
     */
    @GetMapping("/admin/{id}")
    public Result detailAdmin(@PathVariable Long id) {
        VenueDTO dto = venueService.getVenueDetail(id);
        return Result.success(dto);
    }

    /**
     * 管理员新增场馆
     */
    @PostMapping("/admin")
    public Result add(@RequestBody TbVenue venue) {
        venueService.addVenue(venue);
        return Result.success("添加成功");
    }

    /**
     * 管理员编辑场馆
     */
    @PutMapping("/admin")
    public Result update(@RequestBody TbVenue venue) {
        venueService.updateVenue(venue);
        return Result.success("修改成功");
    }

    /**
     * 管理员删除场馆
     */
    @DeleteMapping("/admin/{id}")
    public Result delete(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return Result.success("删除成功");
    }
}
