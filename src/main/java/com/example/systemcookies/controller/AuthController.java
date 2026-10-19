package com.example.systemcookies.controller;

import com.example.systemcookies.common.R;
import com.example.systemcookies.common.UserContext;
import com.example.systemcookies.dto.LoginDTO;
import com.example.systemcookies.dto.RegisterDTO;
import com.example.systemcookies.service.UserService;
import com.example.systemcookies.vo.LoginVO;
import com.example.systemcookies.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册 / 登录 / 退出 / 当前用户
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok("登录成功", userService.login(dto));
    }

    /**
     * 注册（固定注册为顾客）
     */
    @PostMapping("/register")
    public R<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return R.ok("注册成功", userService.register(dto));
    }

    /**
     * 退出登录：删除令牌
     */
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        userService.logout(stripBearer(auth));
        return R.ok();
    }

    /**
     * 当前登录用户
     */
    @GetMapping("/info")
    public R<UserVO> info() {
        return R.ok(userService.current(UserContext.requiredUserId()));
    }

    private String stripBearer(String auth) {
        if (auth == null) {
            return null;
        }
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth;
    }
}
