package com.example.systemcookies.vo;

import lombok.Data;

/**
 * 用户对外信息（不含密码与盐）
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String phone;

    private String avatar;

    /** 角色：0顾客 1管理员 */
    private Integer role;

    private String roleText;
}
