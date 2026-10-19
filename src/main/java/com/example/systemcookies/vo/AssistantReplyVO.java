package com.example.systemcookies.vo;

import lombok.Data;

import java.util.List;

/**
 * 点菜小助理回复
 */
@Data
public class AssistantReplyVO {

    private String sessionId;

    /** 回复文本 */
    private String reply;

    /** 回复来源：ai=阿里云通义千问 / local=本地规则兜底 */
    private String mode;

    /** 本次对话实际执行的动作，便于前端提示与刷新 */
    private List<ActionVO> actions;

    /** 推荐追问，前端渲染成快捷按钮 */
    private List<String> suggestions;

    @Data
    public static class ActionVO {

        /** 动作类型：search / addCart / viewCart / submitOrder */
        private String type;

        /** 动作描述，例如「已加入购物车：红烧肉 × 1」 */
        private String desc;

        /** 下单成功时的订单号 */
        private String orderNo;
    }
}
