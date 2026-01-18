package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.dto.VenueDTO;
import cn.edu.ccst.sims.entity.TbVenue;
import cn.edu.ccst.sims.mapper.TbVenueMapper;
import cn.edu.ccst.sims.service.VenueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {

    @Autowired
    private TbVenueMapper venueMapper;

    /**
     * 场馆列表查询（用户 / 管理员通用）
     */
    @Override
    public Page<VenueDTO> listVenues(Integer pageNum,
            Integer pageSize,
            String type,
            String name,
            Integer status) {

        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        QueryWrapper<TbVenue> wrapper = new QueryWrapper<>();

        if (StringUtils.hasText(type)) {
            wrapper.eq("type", type);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }

        // ============ 修改开始 ============
        // 原逻辑：没有 status 默认查 1
        // 新逻辑：没有 status 就不加限制（查全部），有 status 才查对应的
        if (status != null) {
            wrapper.eq("status", status);
        }
        // 删除 else { wrapper.eq("status", 1); }
        // ============ 修改结束 ============

        wrapper.orderByAsc("price");

        Page<TbVenue> page = venueMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<VenueDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(new ArrayList<>());

        page.getRecords().forEach(venue -> {
            VenueDTO dto = new VenueDTO();
            BeanUtils.copyProperties(venue, dto);
            dtoPage.getRecords().add(dto);
        });

        return dtoPage;
    }

    /**
     * 场馆详情
     */
    @Override
    public VenueDTO getVenueDetail(Long id) {
        TbVenue venue = venueMapper.selectById(id);
        if (venue == null) {
            throw new RuntimeException("场馆不存在");
        }
        VenueDTO dto = new VenueDTO();
        BeanUtils.copyProperties(venue, dto);
        return dto;
    }

    /**
     * 获取推荐场馆
     */
    @Override
    public List<VenueDTO> getRecommendedVenues(Integer limit) {
        // 查询状态为可用(1)的场馆
        QueryWrapper<TbVenue> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只查询可用场馆

        List<TbVenue> venues = venueMapper.selectList(wrapper);

        // 随机打乱场馆列表
        java.util.Collections.shuffle(venues);

        // 手动限制数量
        if (limit != null && limit > 0 && venues.size() > limit) {
            venues = venues.subList(0, limit);
        }

        // 转换为DTO
        List<VenueDTO> result = new ArrayList<>();
        for (TbVenue venue : venues) {
            VenueDTO dto = new VenueDTO();
            BeanUtils.copyProperties(venue, dto);
            result.add(dto);
        }

        return result;
    }

    /**
     * 添加场馆（管理员）
     */
    @Override
    public void addVenue(TbVenue venue) {
        TbVenue exist = venueMapper.selectOne(
                new QueryWrapper<TbVenue>().eq("name", venue.getName()));
        if (exist != null) {
            throw new RuntimeException("场馆名称已存在");
        }
        venueMapper.insert(venue);
    }

    /**
     * 编辑场馆（管理员）
     */
    @Override
    public void updateVenue(TbVenue venue) {
        if (venue.getId() == null) {
            throw new RuntimeException("场馆ID不能为空");
        }
        venueMapper.updateById(venue);
    }

    /**
     * 删除场馆（管理员）
     */
    @Override
    public void deleteVenue(Long id) {

        if (id == null) {
            throw new RuntimeException("场馆ID不能为空");
        }

        int rows = venueMapper.deleteById(id);

        if (rows == 0) {
            throw new RuntimeException("场馆不存在，删除失败");
        }
    }

}
