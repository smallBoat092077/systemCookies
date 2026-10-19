package com.example.systemcookies.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录令牌载荷：存入 Redis，拦截器据此还原用户身份
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {

    /** 用户ID */
    private Long userId;

    /** 角色：0顾客 1管理员 */
    private Integer role;
}
