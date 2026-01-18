package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.dto.FaultReportDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 设施故障管理服务接口
 */
public interface FacilityService {

    /**
     * 申报故障
     * 
     * @param userId         用户ID
     * @param faultReportDTO 故障申报信息
     */
    void reportFault(Long userId, FaultReportDTO faultReportDTO);

    /**
     * 获取故障列表
     * 
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @param status    故障状态
     * @param venueId   场馆ID
     * @param faultType 故障类型
     * @return 故障列表
     */
    Page<Map<String, Object>> getFaultList(Integer pageNum, Integer pageSize,
            Integer status, Long venueId, String faultType);

    /**
     * 获取故障详情
     * 
     * @param id 故障ID
     * @return 故障详情
     */
    Map<String, Object> getFaultDetail(Long id);

    /**
     * 管理员处理故障
     * 
     * @param id            故障ID
     * @param status        处理状态
     * @param processRemark 处理备注
     * @param estimatedTime 预计修复时间
     */
    void processFault(Long id, Integer status, String processRemark, String estimatedTime);

    /**
     * 更新故障进度
     * 
     * @param id       故障ID
     * @param progress 进度描述
     */
    void updateFaultProgress(Long id, String progress);

    /**
     * 用户取消故障申报
     * 
     * @param userId 用户ID
     * @param id     故障ID
     * @param reason 取消原因
     */
    void cancelFault(Long userId, Long id, String reason);

    /**
     * 获取我的故障申报
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   故障状态
     * @return 故障列表
     */
    Page<Map<String, Object>> getMyFaults(Long userId, Integer pageNum, Integer pageSize, Integer status);

    /**
     * 获取故障统计
     * 
     * @param type    统计类型
     * @param venueId 场馆ID
     * @return 统计数据
     */
    Map<String, Object> getFaultStatistics(String type, Long venueId);

    /**
     * 获取设施列表
     * 
     * @param venueId      场馆ID
     * @param facilityType 设施类型
     * @return 设施列表
     */
    List<Map<String, Object>> getFacilityList(Long venueId, String facilityType);

    /**
     * 管理员添加设施
     * 
     * @param facilityData 设施数据
     */
    void addFacility(Map<String, Object> facilityData);

    /**
     * 管理员更新设施
     * 
     * @param id           设施ID
     * @param facilityData 设施数据
     */
    void updateFacility(Long id, Map<String, Object> facilityData);

    /**
     * 管理员删除设施
     * 
     * @param id 设施ID
     */
    void deleteFacility(Long id);
}
