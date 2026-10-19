package com.example.systemcookies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.systemcookies.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 打卡 / 菜谱评论：用户做完某道菜后的图文分享，同时作为菜谱详情页的评论
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_in")
public class CheckIn extends BaseEntity {

    /** 打卡用户ID */
    private Long userId;

    /** 菜谱ID */
    private Long recipeId;

    /** 打卡文字 */
    private String content;

    /** 图片地址，多个用逗号分隔 */
    private String images;
}
