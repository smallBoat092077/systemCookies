package com.example.systemcookies.common;

import lombok.Getter;

/**
 * 业务异常：用于需要向前端明确提示的场景
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
