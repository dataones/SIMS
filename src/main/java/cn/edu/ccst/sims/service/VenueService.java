package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.dto.VenueDTO;
import cn.edu.ccst.sims.entity.TbVenue;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface VenueService {

    // 公共查询（学生/用户）
    Page<VenueDTO> listVenues(Integer pageNum, Integer pageSize, String type, String name, Integer status);

    /**
     * 获取推荐场馆
     * 
     * @param limit 返回数量限制
     * @return 推荐场馆列表
     */
    List<VenueDTO> getRecommendedVenues(Integer limit);

    // ===== 管理员功能 =====

    VenueDTO getVenueDetail(Long id);

    void addVenue(TbVenue venue);

    void updateVenue(TbVenue venue);

    void deleteVenue(Long id);
}
