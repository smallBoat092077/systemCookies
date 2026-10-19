package com.example.systemcookies.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.systemcookies.common.BusinessException;
import com.example.systemcookies.common.CacheSupport;
import com.example.systemcookies.common.PasswordUtil;
import com.example.systemcookies.common.RedisKeys;
import com.example.systemcookies.common.TokenInfo;
import com.example.systemcookies.dto.LoginDTO;
import com.example.systemcookies.dto.RegisterDTO;
import com.example.systemcookies.entity.User;
import com.example.systemcookies.mapper.UserMapper;
import com.example.systemcookies.service.UserService;
import com.example.systemcookies.vo.LoginVO;
import com.example.systemcookies.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

/**
 * 用户与登录服务实现：令牌存 Redis，有效期 7 天
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Duration TOKEN_TTL = Duration.ofDays(7);

    private final CacheSupport cacheSupport;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = lambdaQuery().eq(User::getUsername, dto.getUsername().trim()).one();
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        if (!PasswordUtil.matches(dto.getPassword(), user.getSalt(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        log.info("用户登录：{}（role={}）", user.getUsername(), user.getRole());
        return issueToken(user);
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        String username = dto.getUsername().trim();
        Long exists = lambdaQuery().eq(User::getUsername, username).count();
        if (exists != null && exists > 0) {
            throw new BusinessException("账号已存在，请直接登录");
        }
        String salt = PasswordUtil.generateSalt();
        User user = new User();
        user.setUsername(username);
        user.setSalt(salt);
        user.setPassword(PasswordUtil.hash(dto.getPassword(), salt));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : username);
        user.setPhone(dto.getPhone());
        user.setRole(0);
        user.setStatus(1);
        save(user);
        log.info("用户注册：{}", username);
        return issueToken(user);
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            cacheSupport.delete(RedisKeys.authToken(token));
        }
    }

    @Override
    public UserVO current(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        return toVO(user);
    }

    /** 签发令牌：随机 64 位十六进制串，写入 Redis */
    private LoginVO issueToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        String tokenKey = RedisKeys.authToken(token);
        cacheSupport.set(tokenKey, new TokenInfo(user.getId(), user.getRole()), TOKEN_TTL);
        // 令牌必须真的落盘：缓存不可用时若照常返回令牌，前端首个请求必然 401，
        // 会陷入「登录成功 → 立刻被踢回登录页」的死循环，这里宁可让登录失败
        if (cacheSupport.get(tokenKey, TokenInfo.class) == null) {
            log.error("登录失败：令牌写入缓存后读不到，请检查 Redis 是否可用");
            throw new BusinessException(500, "登录服务暂时不可用，请检查 Redis 后重试");
        }

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpireIn(TOKEN_TTL.getSeconds());
        vo.setUser(toVO(user));
        return vo;
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setRoleText(user.getRole() != null && user.getRole() == 1 ? "管理员" : "顾客");
        return vo;
    }
}
