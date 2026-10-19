package com.example.systemcookies.controller;

import com.example.systemcookies.common.R;
import com.example.systemcookies.common.UserContext;
import com.example.systemcookies.dto.AssistantChatDTO;
import com.example.systemcookies.service.AssistantService;
import com.example.systemcookies.vo.AssistantReplyVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点菜小助理对话接口
 */
@Slf4j
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    /**
     * 对话一轮
     */
    @PostMapping("/chat")
    public R<AssistantReplyVO> chat(@Valid @RequestBody AssistantChatDTO dto) {
        return R.ok(assistantService.chat(UserContext.requiredUserId(), dto));
    }

    /**
     * 清空会话，重新开始
     */
    @DeleteMapping("/session")
    public R<Void> clearSession(@RequestParam(required = false) String sessionId) {
        assistantService.clearSession(UserContext.requiredUserId(), sessionId);
        return R.ok();
    }
}
