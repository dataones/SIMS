package cn.edu.ccst.sims.common;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("成功");
        r.setData(data);
        return r;
    }

    // 成功（不带数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败（只传消息，code 默认 500）
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }

    // 失败（自定义 code 和 msg）
    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }
}