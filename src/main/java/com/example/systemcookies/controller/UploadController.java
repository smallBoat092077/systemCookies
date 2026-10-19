package com.example.systemcookies.controller;

import com.example.systemcookies.common.BusinessException;
import com.example.systemcookies.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 图片上传：打卡配图存本地目录，通过 /uploads/** 静态映射访问
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final long MAX_SIZE = 10 * 1024 * 1024L;
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Value("${app.upload-dir:./uploads/}")
    private String uploadDir;

    /**
     * 上传图片，返回可访问的相对地址
     */
    @PostMapping("/image")
    public R<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片不能超过 10MB");
        }

        try {
            String filename = LocalDate.now().format(DATE_DIR) + "/" + UUID.randomUUID()
                    + resolveExt(file.getOriginalFilename(), contentType);
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = base.resolve(filename).normalize();
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
            log.info("上传图片：{}", target);
            return R.ok(Map.of("url", "/uploads/" + filename.replace("\\", "/")));
        } catch (IOException e) {
            log.error("图片保存失败", e);
            throw new BusinessException("图片保存失败，请稍后重试");
        }
    }

    /** 按原始后缀取扩展名，取不到时按 Content-Type 兜底 */
    private String resolveExt(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                    .toLowerCase(Locale.ROOT);
            if (ext.matches("jpg|jpeg|png|gif|webp|bmp")) {
                return "." + ("jpeg".equals(ext) ? "jpg" : ext);
            }
        }
        if (contentType != null) {
            return switch (contentType) {
                case "image/png" -> ".png";
                case "image/gif" -> ".gif";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };
        }
        return ".jpg";
    }
}
