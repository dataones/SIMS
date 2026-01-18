package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.dto.NoticeDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * 通知公告服务接口
 */
public interface NoticeService {

        /**
         * 发布公告
         * 
         * @param userId    管理员ID
         * @param noticeDTO 公告信息
         */
        void publishNotice(Long userId, NoticeDTO noticeDTO);

        /**
         * 修改公告
         * 
         * @param id        公告ID
         * @param noticeDTO 公告信息
         */
        void updateNotice(Long id, NoticeDTO noticeDTO);

        /**
         * 删除公告
         * 
         * @param id 公告ID
         */
        void deleteNotice(Long id);

        /**
         * 获取公告列表
         * 
         * @param pageNum  页码
         * @param pageSize 每页数量
         * @param keyword  搜索关键词
         * @param type     公告类型
         * @param status   发布状态
         * @param priority 优先级
         * @return 公告列表
         */
        Page<Map<String, Object>> getNoticeList(Integer pageNum, Integer pageSize,
                        String keyword, String type, Integer status, Integer priority);

        /**
         * 发布单个通知
         * 
         * @param id 通知ID
         */
        void publishNotice(Long id);

        /**
         * 获取公告详情
         * 
         * @param id 公告ID
         * @return 公告详情
         */
        Map<String, Object> getNoticeDetail(Long id);

        /**
         * 置顶/取消置顶公告
         * 
         * @param id    公告ID
         * @param isTop 是否置顶
         */
        void toggleTop(Long id, Boolean isTop);

        /**
         * 获取最新公告（首页展示）
         * 
         * @param limit 数量限制
         * @return 最新公告列表
         */
        List<Map<String, Object>> getLatestNotices(Integer limit);

        /**
         * 增加公告阅读量
         * 
         * @param id 公告ID
         */
        void increaseReadCount(Long id);

        /**
         * 管理员获取所有公告
         * 
         * @param pageNum  页码
         * @param pageSize 每页数量
         * @param type     公告类型
         * @param status   状态
         * @param title    标题
         * @return 公告列表
         */
        Page<Map<String, Object>> adminGetNoticeList(Integer pageNum, Integer pageSize,
                        String type, Integer status, String title);
}
