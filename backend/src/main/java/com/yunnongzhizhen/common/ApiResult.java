package com.yunnongzhizhen.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应：{code, message, data}。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private int code;
    private String message;
    private T data;

    /**
     * @param data 业务数据
     * @return 成功响应
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(0, "success", data);
    }

    /**
     * @return 无数据成功响应
     */
    public static ApiResult<Void> ok() {
        return new ApiResult<>(0, "success", null);
    }

    /**
     * @param message 提示信息
     * @return 成功响应
     */
    public static ApiResult<Void> okMessage(String message) {
        return new ApiResult<>(0, message, null);
    }

    /**
     * @param code    错误码
     * @param message 错误信息
     * @return 失败响应
     */
    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
}
