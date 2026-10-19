package com.example.systemcookies.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.systemcookies.dto.LoginDTO;
import com.example.systemcookies.dto.RegisterDTO;
import com.example.systemcookies.entity.User;
import com.example.systemcookies.vo.LoginVO;
import com.example.systemcookies.vo.UserVO;

/**
 * 用户与登录服务
 */
public interface UserService extends IService<User> {

    /**
     * 登录：校验密码后签发令牌
     */
    LoginVO login(LoginDTO dto);

    /**
     * 注册：固定注册为顾客
     */
    LoginVO register(RegisterDTO dto);

    /**
     * 退出登录：删除令牌
     */
    void logout(String token);

    /**
     * 当前登录用户信息
     */
    UserVO current(Long userId);
}
