package com.example.systemcookies.service;

import com.example.systemcookies.dto.AssistantChatDTO;
import com.example.systemcookies.vo.AssistantReplyVO;

/**
 * 点菜小助理：多轮对话 + 工具调用（查菜谱 / 加购 / 看购物车 / 下单）
 */
public interface AssistantService {

    /**
     * 对话一轮，内部保存会话上下文
     */
    AssistantReplyVO chat(Long userId, AssistantChatDTO dto);

    /**
     * 清空会话
     */
    void clearSession(Long userId, String sessionId);
}
