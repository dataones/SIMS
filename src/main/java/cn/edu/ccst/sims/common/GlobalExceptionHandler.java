package cn.edu.ccst.sims.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常（RuntimeException）
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return Result.error(msg);
    }

    /**
     * 兜底异常（防止 500 页面）
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        e.printStackTrace(); // 开发阶段保留，生产可去掉
        return Result.error("系统异常，请联系管理员");
    }
}
