package cn.edu.ccst.sims.mapper;

import cn.edu.ccst.sims.entity.TbVenue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 场馆信息 Mapper 接口
 * 用于场馆资源的CRUD操作，如查询场馆列表、更新场馆状态等。
 */

public interface TbVenueMapper extends BaseMapper<TbVenue> {
    // 可添加自定义，如按类型统计场馆数量，但暂无需。
}