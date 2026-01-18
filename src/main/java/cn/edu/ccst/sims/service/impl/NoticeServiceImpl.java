package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.dto.NoticeDTO;
import cn.edu.ccst.sims.entity.TbNotice;
import cn.edu.ccst.sims.mapper.TbNoticeMapper;
import cn.edu.ccst.sims.service.NoticeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 通知公告服务实现类
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private TbNoticeMapper noticeMapper;

    @Override
    public void publishNotice(Long userId, NoticeDTO noticeDTO) {
        try {
            TbNotice notice = new TbNotice();
            notice.setTitle(noticeDTO.getTitle());
            notice.setContent(noticeDTO.getContent());
            notice.setCreateBy("admin"); // 可以根据userId获取用户名
            notice.setCreateTime(LocalDateTime.now());
            notice.setStatus(1); // 已发布
            notice.setPriority(noticeDTO.getPriority() != null ? noticeDTO.getPriority() : 2); // 默认中优先级
            notice.setIsTop(noticeDTO.getIsTop() != null && noticeDTO.getIsTop() ? 1 : 0); // 默认不置顶
            notice.setReadCount(0); // 初始阅读量为0
            notice.setExpireTime(noticeDTO.getExpireTime()); // 过期时间

            int result = noticeMapper.insert(notice);
            if (result > 0) {
                System.out.println("公告发布成功: " + notice.getTitle() + ", ID: " + notice.getId());
            } else {
                throw new RuntimeException("公告发布失败");
            }
        } catch (Exception e) {
            System.out.println("发布公告失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("发布公告失败: " + e.getMessage());
        }
    }

    @Override
    public void updateNotice(Long id, NoticeDTO noticeDTO) {
        try {
            TbNotice existingNotice = noticeMapper.selectById(id);
            if (existingNotice == null) {
                throw new RuntimeException("公告不存在");
            }

            // 更新字段
            existingNotice.setTitle(noticeDTO.getTitle());
            existingNotice.setContent(noticeDTO.getContent());
            if (noticeDTO.getPriority() != null) {
                existingNotice.setPriority(noticeDTO.getPriority());
            }
            if (noticeDTO.getIsTop() != null) {
                existingNotice.setIsTop(noticeDTO.getIsTop() ? 1 : 0);
            }
            if (noticeDTO.getExpireTime() != null) {
                existingNotice.setExpireTime(noticeDTO.getExpireTime());
            }

            int result = noticeMapper.updateById(existingNotice);
            if (result > 0) {
                System.out.println("公告修改成功: " + existingNotice.getTitle() + ", ID: " + id);
            } else {
                throw new RuntimeException("公告修改失败");
            }
        } catch (Exception e) {
            System.out.println("修改公告失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("修改公告失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteNotice(Long id) {
        try {
            TbNotice notice = noticeMapper.selectById(id);
            if (notice == null) {
                throw new RuntimeException("公告不存在");
            }

            int result = noticeMapper.deleteById(id);
            if (result > 0) {
                System.out.println("公告删除成功: " + notice.getTitle() + ", ID: " + id);
            } else {
                throw new RuntimeException("公告删除失败");
            }
        } catch (Exception e) {
            System.out.println("删除公告失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除公告失败: " + e.getMessage());
        }
    }

    @Override
    public Page<Map<String, Object>> getNoticeList(Integer pageNum, Integer pageSize,
            String keyword, String type, Integer status, Integer priority) {
        System.out.println("=== 开始获取公告列表 ===");
        System.out.println(
                "参数: pageNum=" + pageNum + ", pageSize=" + pageSize + ", keyword=" + keyword + ", type=" + type
                        + ", status=" + status + ", priority=" + priority);

        // 创建TbNotice类型的分页对象
        Page<TbNotice> page = new Page<>(pageNum, pageSize);

        QueryWrapper<TbNotice> wrapper = new QueryWrapper<>();

        // 添加搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("title", keyword)
                    .or()
                    .like("content", keyword));
            System.out.println("添加关键词搜索: " + keyword);
        }

        // 添加类型过滤（虽然数据库中没有type字段，但保留逻辑）
        if (type != null && !type.trim().isEmpty()) {
            // 可以根据priority来模拟type过滤
            if ("urgent".equals(type)) {
                wrapper.eq("priority", 1); // 高优先级
            } else if ("normal".equals(type)) {
                wrapper.eq("priority", 2); // 中优先级
            }
            System.out.println("添加类型过滤: " + type);
        }

        // 添加状态过滤
        if (status != null) {
            wrapper.eq("status", status);
            System.out.println("添加状态过滤: " + status);
        }

        // 添加优先级过滤
        if (priority != null) {
            wrapper.eq("priority", priority);
            System.out.println("添加优先级过滤: " + priority);
        }

        // 按置顶、优先级、创建时间排序
        wrapper.orderByDesc("is_top") // 置顶的在前
                .orderByAsc("priority") // 优先级高的在前（1-高，2-中，3-低）
                .orderByDesc("create_time"); // 时间新的在前

        System.out.println("SQL查询条件: " + wrapper.getCustomSqlSegment());

        try {
            // 执行分页查询
            Page<TbNotice> noticePage = noticeMapper.selectPage(page, wrapper);

            System.out.println("查询结果:");
            System.out.println("总记录数: " + noticePage.getTotal());
            System.out.println("当前页记录数: " + noticePage.getRecords().size());

            // 打印每条记录
            for (TbNotice notice : noticePage.getRecords()) {
                System.out.println("公告ID: " + notice.getId() + ", 标题: " + notice.getTitle() +
                        ", 状态: " + notice.getStatus() + ", 优先级: " + notice.getPriority() + ", 置顶: "
                        + notice.getIsTop());
            }

            // 转换为Map格式
            List<Map<String, Object>> records = new ArrayList<>();
            for (TbNotice notice : noticePage.getRecords()) {
                Map<String, Object> noticeMap = new HashMap<>();
                noticeMap.put("id", notice.getId());
                noticeMap.put("title", notice.getTitle());
                noticeMap.put("content", notice.getContent());
                noticeMap.put("type", "system"); // 默认类型
                noticeMap.put("status", notice.getStatus()); // 使用数据库中的状态
                noticeMap.put("priority", notice.getPriority()); // 使用数据库中的优先级
                noticeMap.put("createBy", notice.getCreateBy());
                noticeMap.put("createTime", notice.getCreateTime());

                // 添加NoticeManage.vue期望的字段
                noticeMap.put("publishTime", notice.getCreateTime());
                noticeMap.put("expireTime", notice.getExpireTime()); // 使用数据库中的过期时间
                noticeMap.put("readCount", notice.getReadCount()); // 使用数据库中的阅读量
                noticeMap.put("isTop", notice.getIsTop() == 1); // 转换为布尔值

                records.add(noticeMap);
            }

            // 设置分页信息
            Page<Map<String, Object>> result = new Page<>(pageNum, pageSize);
            result.setRecords(records);
            result.setTotal(noticePage.getTotal());
            result.setPages(noticePage.getPages());
            result.setCurrent(noticePage.getCurrent());
            result.setSize(noticePage.getSize());

            System.out.println("=== 公告列表获取成功 ===");
            System.out.println("返回记录数: " + records.size());
            System.out.println("返回总记录数: " + result.getTotal());

            return result;
        } catch (Exception e) {
            System.out.println("=== 公告列表获取失败 ===");
            System.out.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void publishNotice(Long id) {
        // TODO: 实现单个通知发布逻辑
        System.out.println("发布通知: " + id);
    }

    @Override
    public Map<String, Object> getNoticeDetail(Long id) {
        try {
            TbNotice notice = noticeMapper.selectById(id);
            if (notice == null) {
                throw new RuntimeException("公告不存在");
            }

            // 转换为Map格式
            Map<String, Object> noticeMap = new HashMap<>();
            noticeMap.put("id", notice.getId());
            noticeMap.put("title", notice.getTitle());
            noticeMap.put("content", notice.getContent());
            noticeMap.put("type", "system"); // 默认类型
            noticeMap.put("status", notice.getStatus());
            noticeMap.put("priority", notice.getPriority());
            noticeMap.put("createBy", notice.getCreateBy());
            noticeMap.put("createTime", notice.getCreateTime());
            noticeMap.put("publishTime", notice.getCreateTime()); // 使用createTime作为发布时间
            noticeMap.put("expireTime", notice.getExpireTime());
            noticeMap.put("readCount", notice.getReadCount());
            noticeMap.put("isTop", notice.getIsTop() == 1);

            System.out.println("获取公告详情成功: ID=" + id + ", 标题=" + notice.getTitle());
            return noticeMap;
        } catch (Exception e) {
            System.out.println("获取公告详情失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取公告详情失败: " + e.getMessage());
        }
    }

    @Override
    public void toggleTop(Long id, Boolean isTop) {
        try {
            TbNotice notice = noticeMapper.selectById(id);
            if (notice == null) {
                throw new RuntimeException("公告不存在");
            }

            notice.setIsTop(isTop ? 1 : 0);

            int result = noticeMapper.updateById(notice);
            if (result > 0) {
                System.out.println("置顶切换成功: " + notice.getTitle() + ", ID: " + id + ", 置顶: " + isTop);
            } else {
                throw new RuntimeException("置顶切换失败");
            }
        } catch (Exception e) {
            System.out.println("切换置顶失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("切换置顶失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getLatestNotices(Integer limit) {
        try {
            QueryWrapper<TbNotice> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1) // 只获取已发布的公告
                    .orderByDesc("is_top") // 置顶的在前
                    .orderByAsc("priority") // 优先级高的在前
                    .orderByDesc("create_time") // 时间新的在前
                    .last("LIMIT " + limit);

            List<TbNotice> notices = noticeMapper.selectList(wrapper);

            List<Map<String, Object>> result = new ArrayList<>();
            for (TbNotice notice : notices) {
                Map<String, Object> noticeMap = new HashMap<>();
                noticeMap.put("id", notice.getId());
                noticeMap.put("title", notice.getTitle());
                noticeMap.put("content", notice.getContent());
                noticeMap.put("status", notice.getStatus());
                noticeMap.put("priority", notice.getPriority());
                noticeMap.put("isTop", notice.getIsTop() == 1);
                noticeMap.put("readCount", notice.getReadCount());
                noticeMap.put("publishTime", notice.getCreateTime());
                noticeMap.put("expireTime", notice.getExpireTime());
                result.add(noticeMap);
            }

            System.out.println("获取最新公告成功，数量: " + result.size());
            return result;
        } catch (Exception e) {
            System.out.println("获取最新公告失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取最新公告失败: " + e.getMessage());
        }
    }

    @Override
    public void increaseReadCount(Long id) {
        try {
            TbNotice notice = noticeMapper.selectById(id);
            if (notice == null) {
                throw new RuntimeException("公告不存在");
            }

            // 增加阅读量
            notice.setReadCount((notice.getReadCount() != null ? notice.getReadCount() : 0) + 1);

            int result = noticeMapper.updateById(notice);
            if (result > 0) {
                System.out.println("增加阅读量成功: 公告ID=" + id + ", 阅读量=" + notice.getReadCount());
            } else {
                throw new RuntimeException("增加阅读量失败");
            }
        } catch (Exception e) {
            System.out.println("增加阅读量失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("增加阅读量失败: " + e.getMessage());
        }
    }

    @Override
    public Page<Map<String, Object>> adminGetNoticeList(Integer pageNum, Integer pageSize,
            String type, Integer status, String title) {
        // TODO: 实现管理员公告列表查询逻辑
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);

        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> notice = new HashMap<>();
            notice.put("id", (long) i);
            notice.put("title", "管理员公告 " + i);
            notice.put("content", "公告内容");
            notice.put("type", type != null ? type : "SYSTEM");
            notice.put("status", status != null ? status : 1);
            notice.put("publishTime", LocalDateTime.now().minusDays(i).toString());
            records.add(notice);
        }

        page.setRecords(records);
        page.setTotal(10);
        return page;
    }
}
