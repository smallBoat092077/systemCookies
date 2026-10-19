package com.example.systemcookies.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发布打卡入参：图片先上传拿到地址，再随打卡提交
 */
@Data
public class CheckInCreateDTO {

    @NotNull(message = "请选择要打卡的菜谱")
    private Long recipeId;

    @Size(max = 500, message = "打卡文字最多 500 字")
    private String content;

    /** 已上传的图片地址，最多 9 张 */
    private List<String> images;
}
