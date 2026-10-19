package com.example.systemcookies.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具：统一 JSON 序列化与前缀批量清理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheSupport {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T get(String key, Class<T> type) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("缓存读取失败 key={}，降级走数据库", key, e);
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> type) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("缓存读取失败 key={}，降级走数据库", key, e);
            return null;
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            log.warn("缓存写入失败 key={}", key, e);
        }
    }

    public void delete(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        stringRedisTemplate.delete(Arrays.asList(keys));
    }

    /**
     * 按前缀批量清理（SCAN 扫描，避免 KEYS 阻塞）
     */
    public void deleteByPrefix(String prefix) {
        try {
            Set<String> keys = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(200).build();
            try (var cursor = stringRedisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
            }
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("按前缀清理缓存失败 prefix={}", prefix, e);
        }
    }

    /**
     * 为空值设置短暂占位，防止缓存击穿
     */
    public void setNullGuard(String key) {
        stringRedisTemplate.opsForValue().set(key, "{}", 60, TimeUnit.SECONDS);
    }
}
