package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.service.NoticeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "公开公告接口")
@RestController
@RequestMapping("/api/notice")
public class PublicNoticeController {

    @Autowired
    private NoticeService noticeService;

    @Operation(summary = "获取公开公告列表")
    @GetMapping("/list")
    public Result<Page<Map<String, Object>>> getPublicNoticeList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 只获取已发布的公告
            Page<Map<String, Object>> page = noticeService.getNoticeList(pageNum, pageSize, null, null, 1, null);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取公告详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getNoticeDetail(@PathVariable Long id) {
        try {
            Map<String, Object> notice = noticeService.getNoticeDetail(id);
            if (notice == null) {
                return Result.error("公告不存在");
            }
            return Result.success(notice);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取最新公告（首页显示）")
    @GetMapping("/latest")
    public Result<List<Map<String, Object>>> getLatestNotices(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "4") Integer limit) {
        try {
            List<Map<String, Object>> notices = noticeService.getLatestNotices(limit);
            return Result.success(notices);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "增加公告阅读量")
    @PutMapping("/{id}/read")
    public Result<String> increaseReadCount(@PathVariable Long id) {
        try {
            noticeService.increaseReadCount(id);
            return Result.success("阅读量增加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
