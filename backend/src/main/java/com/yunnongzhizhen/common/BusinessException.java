package com.yunnongzhizhen.common;

import lombok.Getter;

/**
 * 业务异常，由全局处理器转为统一响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * @param message 错误信息
     */
    public BusinessException(String message) {
        this(400, message);
    }

    /**
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
