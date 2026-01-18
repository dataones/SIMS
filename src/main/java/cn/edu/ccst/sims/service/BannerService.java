package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.entity.TbBanner;
import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService {

    /**
     * 获取启用的轮播图列表
     * 
     * @return 轮播图列表
     */
    List<TbBanner> getActiveBanners();

    /**
     * 获取所有轮播图列表（管理员）
     * 
     * @return 轮播图列表
     */
    List<TbBanner> getAllBanners();

    /**
     * 添加轮播图
     * 
     * @param banner 轮播图信息
     */
    void addBanner(TbBanner banner);

    /**
     * 更新轮播图
     * 
     * @param banner 轮播图信息
     */
    void updateBanner(TbBanner banner);

    /**
     * 删除轮播图
     * 
     * @param id 轮播图ID
     */
    void deleteBanner(Long id);
}
