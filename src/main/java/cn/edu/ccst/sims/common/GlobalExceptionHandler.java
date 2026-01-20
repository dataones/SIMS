package cn.edu.ccst.sims.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * 统一处理Controller层抛出的异常，确保API返回统一的错误格式
 * 
 * 设计原则：
 * 1. 统一异常响应格式
 * 2. 避免暴露系统内部错误信息
 * 3. 提供友好的错误提示
 * 4. 记录异常日志便于调试
 * 
 * 异常处理优先级：
 * 1. 具体异常（如参数校验异常）
 * 2. 业务异常（RuntimeException）
 * 3. 兜底异常（Exception）
 * 
 * @author SIMS开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 
     * 捕获所有RuntimeException及其子类异常
     * 包括：业务逻辑异常、数据校验异常、权限异常等
     * 
     * @param e 业务异常对象
     * @return 统一错误响应Result对象
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        // 直接返回业务异常信息，前端可以据此进行相应处理
        return Result.error(e.getMessage());
    }

    /**
     * 处理参数校验异常
     * 
     * 捕获@Valid注解触发的参数校验失败异常
     * 通常用于Controller方法的@RequestBody或@Validated参数校验
     * 
     * @param e 参数校验异常对象
     * @return 包含具体校验错误信息的Result对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidException(MethodArgumentNotValidException e) {
        // 获取第一个字段的校验错误信息
        String msg = e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return Result.error(msg);
    }

    /**
     * 兜底异常处理器
     * 
     * 捕获所有未被上述方法处理的异常
     * 防止系统异常导致500错误页面，保证API始终返回JSON格式
     * 
     * @param e 异常对象
     * @return 通用错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        // 开发阶段打印异常堆栈，便于调试
        // 生产环境建议使用日志框架记录
        e.printStackTrace();

        // 返回通用错误信息，避免暴露系统内部结构
        return Result.error("系统异常，请联系管理员");
    }
}
