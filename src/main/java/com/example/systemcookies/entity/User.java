package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户：顾客与管理员共用一张表，通过 role 区分
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /** 登录账号 */
    private String username;

    /** 密码摘要（PBKDF2-SHA256） */
    private String password;

    /** 密码盐 */
    private String salt;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 头像 */
    private String avatar;

    /** 角色：0顾客 1管理员 */
    private Integer role;

    /** 状态：1启用 0禁用 */
    private Integer status;
}
