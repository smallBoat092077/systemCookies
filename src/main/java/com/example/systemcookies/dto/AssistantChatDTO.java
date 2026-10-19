package com.example.systemcookies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 点菜小助理对话入参
 */
@Data
public class AssistantChatDTO {

    /** 会话ID，由前端生成并持续携带，用于多轮对话 */
    private String sessionId;

    @NotBlank(message = "请输入内容")
    @Size(max = 500, message = "内容最多 500 字")
    private String message;
}
