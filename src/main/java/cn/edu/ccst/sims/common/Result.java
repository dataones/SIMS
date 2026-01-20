package cn.edu.ccst.sims.common;

import lombok.Data;

/**
 * 统一API响应结果封装类
 * 
 * 设计目标：
 * 1. 统一所有API接口的响应格式
 * 2. 提供泛型支持，方便返回不同类型的数据
 * 3. 包含状态码、消息和数据三个核心字段
 * 4. 支持链式调用和快速构建
 * 
 * 响应格式：
 * {
 * "code": 200, // 状态码：200成功，400客户端错误，500服务器错误
 * "msg": "成功", // 响应消息：操作结果的文字描述
 * "data": {...} // 响应数据：实际返回的业务数据
 * }
 * 
 * 状态码规范：
 * 200 - 操作成功
 * 400 - 参数错误
 * 401 - 未认证
 * 403 - 权限不足
 * 404 - 资源不存在
 * 500 - 服务器内部错误
 * 
 * @param <T> 泛型类型，表示返回数据的类型
 * @author SIMS开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class Result<T> {

    /**
     * 响应状态码
     * 200: 成功
     * 400: 客户端错误
     * 401: 未认证
     * 403: 权限不足
     * 404: 资源不存在
     * 500: 服务器错误
     */
    private int code;

    /**
     * 响应消息
     * 用于描述操作结果，便于前端显示给用户
     */
    private String msg;

    /**
     * 响应数据
     * 泛型类型，可以是任意业务对象、集合或基本类型
     */
    private T data;

    /**
     * 成功响应（带数据）
     * 
     * @param data 要返回的业务数据
     * @param <T>  数据类型
     * @return Result对象，code=200，msg="成功"
     * 
     *         使用示例：
     *         return Result.success(userList);
     *         return Result.success(orderDetail);
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("成功");
        r.setData(data);
        return r;
    }

    /**
     * 成功响应（不带数据）
     * 
     * @param <T> 数据类型
     * @return Result对象，code=200，msg="成功"，data=null
     * 
     *         使用示例：
     *         return Result.success(); // 用于删除、更新等操作
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应（默认状态码）
     * 
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return Result对象，code=500，msg=传入的消息，data=null
     * 
     *         使用示例：
     *         return Result.error("用户名已存在");
     *         return Result.error("订单不存在");
     */
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }

    /**
     * 失败响应（自定义状态码）
     * 
     * @param code 自定义状态码
     * @param msg  错误消息
     * @param <T>  数据类型
     * @return Result对象，code和msg为传入值，data=null
     * 
     *         使用示例：
     *         return Result.error(400, "参数校验失败");
     *         return Result.error(404, "用户不存在");
     */
    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }
}