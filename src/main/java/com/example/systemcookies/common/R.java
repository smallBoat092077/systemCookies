package com.example.systemcookies.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 统一接口响应体
 *
 * @param <T> 数据泛型
 */
@Data
@Accessors(chain = true)
public class R<T> implements Serializable {

    /** 业务状态码：200 成功，其它为失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /** 响应时间戳 */
    private Long timestamp;

    public static <T> R<T> ok() {
        return build(200, "操作成功", null);
    }

    public static <T> R<T> ok(T data) {
        return build(200, "操作成功", data);
    }

    public static <T> R<T> ok(String message, T data) {
        return build(200, message, data);
    }

    public static <T> R<T> fail(String message) {
        return build(500, message, null);
    }

    public static <T> R<T> fail(Integer code, String message) {
        return build(code, message, null);
    }

    private static <T> R<T> build(Integer code, String message, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(data);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }
}
