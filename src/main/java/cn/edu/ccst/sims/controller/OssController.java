package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.utils.OssUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OSS文件上传控制器
 */
@RestController
@RequestMapping("/api/oss")
public class OssController {

    @Autowired
    private OssUtil ossUtil;

    /**
     * 获取OSS上传签名
     */
    @Operation(summary = "获取OSS上传签名")
    @GetMapping("/signature")
    public Result<Map<String, Object>> getSignature() {
        try {
            Map<String, Object> signature = ossUtil.generateSignature();
            return Result.success(signature);
        } catch (Exception e) {
            return Result.error("获取签名失败: " + e.getMessage());
        }
    }

    /**
     * 测试OSS上传
     */
    @Operation(summary = "测试OSS上传")
    @PostMapping("/test")
    public Result<String> testUpload(@RequestParam String url) {
        return Result.success("OSS上传测试成功，URL: " + url);
    }
}
