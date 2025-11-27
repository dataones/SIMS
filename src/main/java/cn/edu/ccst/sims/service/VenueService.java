package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.dto.VenueDTO;
import cn.edu.ccst.sims.entity.TbVenue;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface VenueService {

    // 公共查询（学生/用户）
    Page<VenueDTO> listVenues(Integer pageNum, Integer pageSize, String type, String name, Integer status);

    // ===== 管理员功能 =====

    VenueDTO getVenueDetail(Long id);

    void addVenue(TbVenue venue);

    void updateVenue(TbVenue venue);

    void deleteVenue(Long id);
}
