package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.dto.FaultReportDTO;
import cn.edu.ccst.sims.entity.TbFaultReport;
import cn.edu.ccst.sims.entity.TbVenue;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.TbFaultReportMapper;
import cn.edu.ccst.sims.mapper.TbVenueMapper;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import cn.edu.ccst.sims.service.FacilityService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 设施服务实现类
 */
@Service
public class FacilityServiceImpl extends ServiceImpl<TbFaultReportMapper, TbFaultReport> implements FacilityService {

    @Autowired
    private TbFaultReportMapper faultReportMapper;

    @Autowired
    private TbVenueMapper venueMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public void reportFault(Long userId, FaultReportDTO faultReportDTO) {
        // 创建故障记录
        TbFaultReport faultReport = new TbFaultReport();
        BeanUtils.copyProperties(faultReportDTO, faultReport);
        faultReport.setUserId(userId);
        faultReport.setCreateTime(LocalDateTime.now());

        // 保存到数据库
        faultReportMapper.insert(faultReport);

        System.out.println("用户 " + userId + " 申报故障: " + faultReportDTO.getTitle());
    }

    @Override
    public Page<Map<String, Object>> getFaultList(Integer pageNum, Integer pageSize,
            Integer status, Long venueId, String faultType) {
        System.out.println("=== 开始查询故障列表 ===");

        try {
            // 先测试简单查询
            List<TbFaultReport> allFaults = faultReportMapper.selectList(null);
            System.out.println("数据库中总共有 " + allFaults.size() + " 条故障记录");

            // 如果没有数据，尝试插入一条测试数据
            if (allFaults.isEmpty()) {
                System.out.println("数据库为空，尝试插入测试数据...");
                TbFaultReport testFault = new TbFaultReport();
                testFault.setUserId(1L);
                testFault.setVenueId(1L);
                testFault.setTitle("测试故障");
                testFault.setContent("这是一个测试故障");
                testFault.setUrgency("MEDIUM");
                testFault.setLocation("测试位置");
                testFault.setFaultType("场地设施");
                testFault.setStatus(0);
                testFault.setCreateTime(LocalDateTime.now());
                faultReportMapper.insert(testFault);
                System.out.println("测试数据插入完成");

                // 重新查询
                allFaults = faultReportMapper.selectList(null);
                System.out.println("插入后数据库中有 " + allFaults.size() + " 条故障记录");
            }

            Page<TbFaultReport> page = new Page<>(pageNum, pageSize);

            // 构建查询条件
            QueryWrapper<TbFaultReport> queryWrapper = new QueryWrapper<>();
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            if (venueId != null) {
                queryWrapper.eq("venue_id", venueId);
            }
            queryWrapper.orderByDesc("create_time");

            System.out.println("查询条件: status=" + status + ", venueId=" + venueId);
            System.out.println("SQL: " + queryWrapper.getCustomSqlSegment());

            // 查询数据
            IPage<TbFaultReport> faultPage = faultReportMapper.selectPage(page, queryWrapper);

            System.out.println("查询结果总数: " + faultPage.getTotal());
            System.out.println("查询结果记录数: " + faultPage.getRecords().size());

            // 转换为前端需要的格式
            List<Map<String, Object>> records = new ArrayList<>();
            for (TbFaultReport fault : faultPage.getRecords()) {
                Map<String, Object> faultMap = new HashMap<>();
                faultMap.put("id", fault.getId());
                faultMap.put("userId", fault.getUserId());
                faultMap.put("venueId", fault.getVenueId());
                faultMap.put("title", fault.getTitle());
                faultMap.put("content", fault.getContent());
                faultMap.put("urgency", fault.getUrgency());
                faultMap.put("location", fault.getLocation());
                faultMap.put("faultType", fault.getFaultType());
                faultMap.put("status", fault.getStatus());
                faultMap.put("result", fault.getResult());
                faultMap.put("createTime", fault.getCreateTime());
                // faultMap.put("updateTime", fault.getUpdateTime());

                // 获取场馆名称
                if (fault.getVenueId() != null) {
                    TbVenue venue = venueMapper.selectById(fault.getVenueId());
                    faultMap.put("venueName", venue != null ? venue.getName() : "未知场馆");
                } else {
                    faultMap.put("venueName", "未指定场馆");
                }

                // 获取申报人姓名
                SysUser user = userMapper.selectById(fault.getUserId());
                faultMap.put("reporterName", user != null ? user.getNickname() : "未知用户");
                faultMap.put("contactPhone", user != null ? user.getPhone() : "");

                records.add(faultMap);
            }

            Page<Map<String, Object>> result = new Page<>(pageNum, pageSize);
            result.setRecords(records);
            result.setTotal(faultPage.getTotal());

            System.out.println("=== 查询完成 ===");
            return result;

        } catch (Exception e) {
            System.err.println("查询故障列表时发生错误: " + e.getMessage());
            e.printStackTrace();
            // 返回空结果
            Page<Map<String, Object>> emptyResult = new Page<>(pageNum, pageSize);
            emptyResult.setRecords(new ArrayList<>());
            emptyResult.setTotal(0L);
            return emptyResult;
        }
    }

    @Override
    public Map<String, Object> getFaultDetail(Long id) {
        try {
            TbFaultReport faultReport = faultReportMapper.selectById(id);
            if (faultReport == null) {
                return new HashMap<>();
            }

            Map<String, Object> faultMap = new HashMap<>();
            faultMap.put("id", faultReport.getId());
            faultMap.put("userId", faultReport.getUserId());
            faultMap.put("venueId", faultReport.getVenueId());
            faultMap.put("title", faultReport.getTitle());
            faultMap.put("content", faultReport.getContent());
            faultMap.put("urgency", faultReport.getUrgency());
            faultMap.put("location", faultReport.getLocation());
            faultMap.put("faultType", faultReport.getFaultType());
            faultMap.put("status", faultReport.getStatus());
            faultMap.put("result", faultReport.getResult());
            faultMap.put("createTime", faultReport.getCreateTime());

            // 获取场馆名称
            if (faultReport.getVenueId() != null) {
                TbVenue venue = venueMapper.selectById(faultReport.getVenueId());
                faultMap.put("venueName", venue != null ? venue.getName() : "未知场馆");
            } else {
                faultMap.put("venueName", "未指定场馆");
            }

            // 获取申报人姓名
            SysUser user = userMapper.selectById(faultReport.getUserId());
            faultMap.put("reporterName", user != null ? user.getNickname() : "未知用户");
            faultMap.put("contactPhone", user != null ? user.getPhone() : "");

            return faultMap;
        } catch (Exception e) {
            System.err.println("查询故障详情时发生错误: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public void processFault(Long id, Integer status, String processRemark, String estimatedTime) {
        try {
            // 更新故障状态
            TbFaultReport faultReport = new TbFaultReport();
            faultReport.setId(id);
            faultReport.setStatus(status);
            faultReport.setResult(processRemark);

            faultReportMapper.updateById(faultReport);

            System.out.println("处理故障 " + id + ", 状态: " + status + ", 备注: " + processRemark);
        } catch (Exception e) {
            System.err.println("处理故障时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void updateFaultProgress(Long id, String progress) {
        try {
            // 更新故障处理进度
            TbFaultReport faultReport = new TbFaultReport();
            faultReport.setId(id);
            faultReport.setResult(progress);

            faultReportMapper.updateById(faultReport);

            System.out.println("更新故障 " + id + " 进度: " + progress);
        } catch (Exception e) {
            System.err.println("更新故障进度时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void cancelFault(Long userId, Long id, String reason) {
        try {
            // 取消故障（只能取消自己的故障）
            TbFaultReport faultReport = faultReportMapper.selectById(id);
            if (faultReport != null && faultReport.getUserId().equals(userId)) {
                faultReport.setStatus(3); // 3-已取消
                faultReport.setResult("用户取消：" + reason);

                faultReportMapper.updateById(faultReport);
            }

            System.out.println("用户 " + userId + " 取消故障 " + id + ", 原因: " + reason);
        } catch (Exception e) {
            System.err.println("取消故障时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Page<Map<String, Object>> getMyFaults(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        Page<TbFaultReport> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        QueryWrapper<TbFaultReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");

        // 查询数据
        IPage<TbFaultReport> faultPage = faultReportMapper.selectPage(page, queryWrapper);

        // 转换为前端需要的格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (TbFaultReport fault : faultPage.getRecords()) {
            Map<String, Object> faultMap = new HashMap<>();
            faultMap.put("id", fault.getId());
            faultMap.put("userId", fault.getUserId());
            faultMap.put("venueId", fault.getVenueId());
            faultMap.put("title", fault.getTitle());
            faultMap.put("content", fault.getContent());
            faultMap.put("urgency", fault.getUrgency());
            faultMap.put("location", fault.getLocation());
            faultMap.put("faultType", fault.getFaultType());
            faultMap.put("status", fault.getStatus());
            faultMap.put("result", fault.getResult());
            faultMap.put("createTime", fault.getCreateTime());

            // 获取场馆名称
            if (fault.getVenueId() != null) {
                TbVenue venue = venueMapper.selectById(fault.getVenueId());
                faultMap.put("venueName", venue != null ? venue.getName() : "未知场馆");
            } else {
                faultMap.put("venueName", "未指定场馆");
            }

            records.add(faultMap);
        }

        Page<Map<String, Object>> result = new Page<>(pageNum, pageSize);
        result.setRecords(records);
        result.setTotal(faultPage.getTotal());

        return result;
    }

    @Override
    public Map<String, Object> getFaultStatistics(String type, Long venueId) {
        try {
            System.out.println("=== 开始查询故障统计 ===");

            // 按状态统计
            QueryWrapper<TbFaultReport> statusWrapper = new QueryWrapper<>();
            if (venueId != null) {
                statusWrapper.eq("venue_id", venueId);
            }

            List<TbFaultReport> allFaults = faultReportMapper.selectList(statusWrapper);
            System.out.println("数据库中总共有 " + allFaults.size() + " 条故障记录");

            Map<String, Object> statusStats = new HashMap<>();
            statusStats.put("pending", 0); // 待处理
            statusStats.put("processing", 0); // 处理中
            statusStats.put("resolved", 0); // 已修复

            for (TbFaultReport fault : allFaults) {
                switch (fault.getStatus()) {
                    case 0:
                        statusStats.put("pending", (Integer) statusStats.get("pending") + 1);
                        break;
                    case 1:
                        statusStats.put("processing", (Integer) statusStats.get("processing") + 1);
                        break;
                    case 2:
                        statusStats.put("resolved", (Integer) statusStats.get("resolved") + 1);
                        break;
                }
            }

            System.out.println("统计结果: " + statusStats);
            System.out.println("=== 统计完成 ===");

            return statusStats;
        } catch (Exception e) {
            System.err.println("查询故障统计时发生错误: " + e.getMessage());
            e.printStackTrace();

            // 返回默认值
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("pending", 0);
            defaultStats.put("processing", 0);
            defaultStats.put("resolved", 0);
            return defaultStats;
        }
    }

    @Override
    public List<Map<String, Object>> getFacilityList(Long venueId, String facilityType) {
        // 这里需要实现真实的设施查询，暂时返回空列表
        // 因为tb_facility表可能还没有对应的Mapper
        return new ArrayList<>();
    }

    @Override
    public void addFacility(Map<String, Object> facilityData) {
        // TODO: 实现设施添加逻辑
        System.out.println("添加设施: " + facilityData);
    }

    @Override
    public void updateFacility(Long id, Map<String, Object> facilityData) {
        // TODO: 实现设施更新逻辑
        System.out.println("更新设施 " + id + ": " + facilityData);
    }

    @Override
    public void deleteFacility(Long id) {
        // TODO: 实现设施删除逻辑
        System.out.println("删除设施: " + id);
    }
}
