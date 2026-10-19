package com.example.systemcookies.controller;

import com.example.systemcookies.common.PageResult;
import com.example.systemcookies.common.R;
import com.example.systemcookies.common.UserContext;
import com.example.systemcookies.dto.CheckInCreateDTO;
import com.example.systemcookies.dto.CheckInReplyCreateDTO;
import com.example.systemcookies.service.CheckInReplyService;
import com.example.systemcookies.service.CheckInService;
import com.example.systemcookies.vo.CheckInReplyVO;
import com.example.systemcookies.vo.CheckInVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 打卡 / 菜谱评论接口：评论列表公开，发布与删除需登录
 */
@RestController
@RequestMapping("/api/check-in")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final CheckInReplyService checkInReplyService;

    /**
     * 发布打卡
     */
    @PostMapping
    public R<Map<String, Long>> create(@Valid @RequestBody CheckInCreateDTO dto) {
        Long id = checkInService.create(UserContext.requiredUserId(), dto);
        return R.ok("打卡成功", Map.of("id", id));
    }

    /**
     * 删除自己的打卡
     */
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        checkInService.removeOwn(UserContext.requiredUserId(), id);
        return R.ok();
    }

    /**
     * 某道菜的评论列表（公开，按时间倒序）
     */
    @GetMapping("/recipe/{recipeId}")
    public R<PageResult<CheckInVO>> pageByRecipe(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 未登录也能看评论，此时不标记「本人」
        Long currentUserId = UserContext.getUserId();
        return R.ok(checkInService.pageByRecipe(recipeId, pageNum, pageSize, currentUserId));
    }

    /**
     * 某道菜的评论总数
     */
    @GetMapping("/recipe/{recipeId}/count")
    public R<Map<String, Long>> countByRecipe(@PathVariable Long recipeId) {
        return R.ok(Map.of("count", checkInService.countByRecipe(recipeId)));
    }

    /**
     * 我的打卡
     */
    @GetMapping("/mine")
    public R<PageResult<CheckInVO>> pageMine(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(checkInService.pageMine(UserContext.requiredUserId(), pageNum, pageSize));
    }

    /**
     * 今日打卡次数
     */
    @GetMapping("/today-count")
    public R<Map<String, Long>> todayCount() {
        return R.ok(Map.of("count", checkInService.todayCount(UserContext.requiredUserId())));
    }

    /**
     * 某条打卡的回复列表（公开，按时间正序）
     */
    @GetMapping("/{checkInId}/replies")
    public R<PageResult<CheckInReplyVO>> pageReplies(
            @PathVariable Long checkInId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(checkInReplyService.pageByCheckIn(checkInId, pageNum, pageSize, UserContext.getUserId()));
    }

    /**
     * 回复某条打卡，需登录
     */
    @PostMapping("/{checkInId}/replies")
    public R<Map<String, Long>> createReply(
            @PathVariable Long checkInId, @Valid @RequestBody CheckInReplyCreateDTO dto) {
        Long id = checkInReplyService.create(UserContext.requiredUserId(), checkInId, dto);
        return R.ok("回复成功", Map.of("id", id));
    }

    /**
     * 删除自己的回复
     */
    @DeleteMapping("/replies/{id}")
    public R<Void> removeReply(@PathVariable Long id) {
        checkInReplyService.removeOwn(UserContext.requiredUserId(), id);
        return R.ok();
    }

    /**
     * 我收到的回复：别人回复了我发布的打卡
     */
    @GetMapping("/replies/inbox")
    public R<PageResult<CheckInReplyVO>> inbox(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(checkInReplyService.pageInbox(UserContext.requiredUserId(), pageNum, pageSize));
    }

    /**
     * 我发出的回复
     */
    @GetMapping("/replies/mine")
    public R<PageResult<CheckInReplyVO>> myReplies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(checkInReplyService.pageMine(UserContext.requiredUserId(), pageNum, pageSize));
    }
}
