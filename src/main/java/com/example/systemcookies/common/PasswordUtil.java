package com.example.systemcookies.common;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 密码工具：PBKDF2WithHmacSHA256（JDK 内置实现，无需额外依赖）
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 30000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 8;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private PasswordUtil() {
    }

    /** 生成 16 位十六进制盐 */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return HEX.formatHex(salt);
    }

    /** 计算密码摘要 */
    public static String hash(String rawPassword, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    rawPassword.toCharArray(),
                    salt.getBytes(StandardCharsets.UTF_8),
                    ITERATIONS,
                    KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return HEX.formatHex(factory.generateSecret(spec).getEncoded());
        } catch (Exception e) {
            throw new BusinessException("密码加密失败");
        }
    }

    /** 定长比较，避免时序侧信道 */
    public static boolean matches(String rawPassword, String salt, String expected) {
        if (rawPassword == null || salt == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(rawPassword, salt).getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
