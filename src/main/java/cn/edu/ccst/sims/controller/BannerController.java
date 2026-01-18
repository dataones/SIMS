package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbBanner;
import cn.edu.ccst.sims.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取启用的轮播图列表（公开接口）
     */
    @GetMapping("/active")
    public Result getActiveBanners() {
        List<TbBanner> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }

    /**
     * 获取所有轮播图列表（管理员接口）
     */
    @GetMapping("/list")
    public Result getAllBanners() {
        List<TbBanner> banners = bannerService.getAllBanners();
        return Result.success(banners);
    }

    /**
     * 添加轮播图（管理员接口）
     */
    @PostMapping("/admin")
    public Result addBanner(@RequestBody TbBanner banner) {
        try {
            bannerService.addBanner(banner);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新轮播图（管理员接口）
     */
    @PutMapping("/admin")
    public Result updateBanner(@RequestBody TbBanner banner) {
        try {
            bannerService.updateBanner(banner);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除轮播图（管理员接口）
     */
    @DeleteMapping("/admin/{id}")
    public Result deleteBanner(@PathVariable Long id) {
        try {
            bannerService.deleteBanner(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
