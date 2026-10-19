package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 打卡回复：用户对某条打卡（评论）的回复，支持互相回复
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_in_reply")
public class CheckInReply extends BaseEntity {

    /** 所属打卡ID */
    private Long checkInId;

    /** 回复人ID */
    private Long userId;

    /** 被回复人ID，为空表示回复打卡作者 */
    private Long replyToUserId;

    /** 回复内容 */
    private String content;
}
