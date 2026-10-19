package com.example.systemcookies.vo;

import lombok.Data;

import java.util.List;

/**
 * 打卡 / 评论展示：同时带用户信息与菜谱快照
 */
@Data
public class CheckInVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private Long recipeId;

    private String recipeName;

    private String recipeCover;

    /** 打卡文字 */
    private String content;

    /** 图片地址列表 */
    private List<String> images;

    /** 格式化后的时间：yyyy-MM-dd HH:mm */
    private String createTime;

    /** 该条打卡收到的回复数 */
    private Long replyCount;

    /** 是否为当前登录用户本人发布，前端据此显示删除 */
    private Boolean mine;
}
