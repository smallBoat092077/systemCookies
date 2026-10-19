package com.example.systemcookies.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 启动自检：Redis 是登录令牌的存储，不可用会导致「登录成功却立刻被踢」。
 * 这里启动时就大声报错，避免问题被拖到页面上才暴露。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupHealthCheck implements ApplicationRunner {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            log.info("启动自检：Redis 可用（{}）", pong);
        } catch (Exception e) {
            log.error("=========================================================");
            log.error("启动自检失败：Redis 不可用！原因：{}", e.getMessage());
            log.error("登录会一直失败（令牌无法保存），请先启动 Redis 再启动本项目。");
            log.error("=========================================================");
        }
    }
}
