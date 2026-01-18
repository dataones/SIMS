package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.entity.TbBanner;
import cn.edu.ccst.sims.mapper.TbBannerMapper;
import cn.edu.ccst.sims.service.BannerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private TbBannerMapper bannerMapper;

    @Override
    public List<TbBanner> getActiveBanners() {
        QueryWrapper<TbBanner> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
                .orderByDesc("sort_order")
                .orderByDesc("create_time");
        return bannerMapper.selectList(wrapper);
    }

    @Override
    public List<TbBanner> getAllBanners() {
        QueryWrapper<TbBanner> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("sort_order")
                .orderByDesc("create_time");
        return bannerMapper.selectList(wrapper);
    }

    @Override
    public void addBanner(TbBanner banner) {
        bannerMapper.insert(banner);
    }

    @Override
    public void updateBanner(TbBanner banner) {
        if (banner.getId() == null) {
            throw new RuntimeException("轮播图ID不能为空");
        }
        bannerMapper.updateById(banner);
    }

    @Override
    public void deleteBanner(Long id) {
        if (id == null) {
            throw new RuntimeException("轮播图ID不能为空");
        }
        int rows = bannerMapper.deleteById(id);
        if (rows == 0) {
            throw new RuntimeException("轮播图不存在，删除失败");
        }
    }
}
