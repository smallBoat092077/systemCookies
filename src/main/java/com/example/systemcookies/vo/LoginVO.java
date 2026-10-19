package com.example.systemcookies.vo;

import lombok.Data;

/**
 * 登录结果：令牌 + 用户信息
 */
@Data
public class LoginVO {

    /** 登录令牌，前端放入 Authorization 请求头 */
    private String token;

    /** 令牌有效期（秒） */
    private Long expireIn;

    /** 用户信息 */
    private UserVO user;
}
