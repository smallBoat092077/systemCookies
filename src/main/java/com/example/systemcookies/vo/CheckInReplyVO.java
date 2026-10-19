package com.example.systemcookies.vo;

import lombok.Data;

/**
 * 打卡回复展示：带回复人与被回复人信息，收件箱场景还带原打卡上下文
 */
@Data
public class CheckInReplyVO {

    private Long id;

    private Long checkInId;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    /** 被回复人ID，为空表示回复的是打卡作者 */
    private Long replyToUserId;

    private String replyToNickname;

    private String content;

    /** 格式化后的时间：yyyy-MM-dd HH:mm */
    private String createTime;

    /** 是否为当前登录用户发出 */
    private Boolean mine;

    /** 原打卡内容（收件箱用） */
    private String checkInContent;

    /** 原打卡所属菜谱 */
    private Long checkInRecipeId;

    private String checkInRecipeName;
}
