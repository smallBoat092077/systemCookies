package com.example.systemcookies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 回复打卡入参
 */
@Data
public class CheckInReplyCreateDTO {

    @NotBlank(message = "请输入回复内容")
    @Size(max = 500, message = "回复最多 500 字")
    private String content;

    /** 被回复人ID，为空表示回复打卡作者 */
    private Long replyToUserId;
}
